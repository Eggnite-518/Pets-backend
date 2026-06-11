package com.example.pets_backend.frameworks.auth;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    private static final String USER_ID_KEY = "userId";
    private static final String PHONE_KEY = "phone";
    private static final String NICKNAME_KEY = "nickname";
    private static final String ROLE_TYPE_KEY = "roleType";

    private final JwtProperties jwtProperties;
    private final Key signingKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserInfoDTO userInfo) {
        if (userInfo == null || userInfo.userId() == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getTtlSeconds() * 1000L);
        return Jwts.builder()
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .claim(USER_ID_KEY, String.valueOf(userInfo.userId()))
                .claim(PHONE_KEY, userInfo.phone())
                .claim(NICKNAME_KEY, userInfo.nickname())
                .claim(ROLE_TYPE_KEY, Objects.toString(userInfo.roleType(), null))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public UserInfoDTO parseToken(String token) {
        String compactToken = resolveToken(token);
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(compactToken);
            Claims claims = jws.getBody();
            return new UserInfoDTO(
                    parseLongClaim(claims.get(USER_ID_KEY, String.class)),
                    claims.get(PHONE_KEY, String.class),
                    claims.get(NICKNAME_KEY, String.class),
                    parseIntegerClaim(claims.get(ROLE_TYPE_KEY, String.class)),
                    compactToken);
        } catch (ExpiredJwtException exception) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_EXPIRED_ERROR);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
    }

    private String resolveToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_MISSING_ERROR);
        }
        String compactToken = token.trim();
        if (compactToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            compactToken = compactToken.substring(7).trim();
        }
        if (!StringUtils.hasText(compactToken)) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_MISSING_ERROR);
        }
        return compactToken;
    }

    private Long parseLongClaim(String claimValue) {
        if (!StringUtils.hasText(claimValue)) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        try {
            return Long.valueOf(claimValue);
        } catch (NumberFormatException exception) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
    }

    private Integer parseIntegerClaim(String claimValue) {
        if (!StringUtils.hasText(claimValue)) {
            return null;
        }
        try {
            return Integer.valueOf(claimValue);
        } catch (NumberFormatException exception) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
    }
}

