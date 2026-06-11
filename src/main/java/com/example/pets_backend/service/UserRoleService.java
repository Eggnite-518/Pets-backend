package com.example.pets_backend.service;

import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.UpgradeCaretakerRoleRespDTO;
import com.example.pets_backend.enums.UserRoleTypeEnum;
import com.example.pets_backend.frameworks.auth.JwtUtil;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private static final int ROLE_PET_OWNER = 1;
    private static final int ROLE_SITTER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int VERIFY_STATUS_INIT = 0;
    private static final int DEFAULT_CREDIT_SCORE = 80;
    private static final int DEFAULT_SERVICE_RANGE_KM = 5;

    private final UserDao userDao;
    private final SitterProfileDao sitterProfileDao;
    private final SitterTrainingRecordDao sitterTrainingRecordDao;
    private final JwtUtil jwtUtil;

    @Transactional
    public UpgradeCaretakerRoleRespDTO upgradeCurrentUserToCaretakerTemporarily() {
        Long userId = currentUserId();
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        Integer nextRoleType = resolveNextRoleType(user.getRoleType());
        if (!nextRoleType.equals(user.getRoleType())) {
            user.setRoleType(nextRoleType);
            userDao.updateById(user);
        }

        SitterProfileDO profile = ensureSitterProfile(userId);
        ensureTrainingRecord(userId);

        String token = jwtUtil.generateAccessToken(new UserInfoDTO(
                user.getUserId(),
                user.getPhone(),
                user.getNickname(),
                nextRoleType,
                null));

        return new UpgradeCaretakerRoleRespDTO(
                user.getUserId(),
                nextRoleType,
                UserRoleTypeEnum.getDescByCode(nextRoleType),
                token,
                profile.getVerifyStatus());
    }

    private Integer resolveNextRoleType(Integer currentRoleType) {
        if (currentRoleType == null || currentRoleType == ROLE_PET_OWNER) {
            return ROLE_BOTH;
        }
        if (currentRoleType == ROLE_SITTER || currentRoleType == ROLE_BOTH) {
            return ROLE_BOTH;
        }
        throw new ClientException(BaseErrorCode.CLIENT_ERROR);
    }

    private SitterProfileDO ensureSitterProfile(Long userId) {
        SitterProfileDO profile = sitterProfileDao.selectById(userId);
        if (profile != null) {
            return profile;
        }
        profile = new SitterProfileDO();
        profile.setProviderId(userId);
        profile.setVerifyStatus(VERIFY_STATUS_INIT);
        profile.setDepositAmount(BigDecimal.ZERO);
        profile.setCreditScore(DEFAULT_CREDIT_SCORE);
        profile.setIsBanned(0);
        profile.setServiceRadiusKm(DEFAULT_SERVICE_RANGE_KM);
        sitterProfileDao.insert(profile);
        return profile;
    }

    private void ensureTrainingRecord(Long userId) {
        SitterTrainingRecordDO record = sitterTrainingRecordDao.selectById(userId);
        if (record != null) {
            return;
        }
        record = new SitterTrainingRecordDO();
        record.setProviderId(userId);
        sitterTrainingRecordDao.insert(record);
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}
