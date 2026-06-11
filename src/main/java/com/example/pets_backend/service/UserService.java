package com.example.pets_backend.service;

import com.example.pets_backend.dao.ArbitrationRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SmsVerificationCodeDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SmsVerificationCodeDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.ChangePasswordReqDTO;
import com.example.pets_backend.dto.req.LoginByCodeReqDTO;
import com.example.pets_backend.dto.req.LoginUserReqDTO;
import com.example.pets_backend.dto.req.RegisterUserReqDTO;
import com.example.pets_backend.dto.req.SetPasswordReqDTO;
import com.example.pets_backend.dto.resp.LoginUserRespDTO;
import com.example.pets_backend.dto.resp.RegisterUserRespDTO;
import com.example.pets_backend.enums.UserRoleTypeEnum;
import com.example.pets_backend.frameworks.auth.JwtUtil;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import com.example.pets_backend.service.encoder.PasswordEncoder;
import com.example.pets_backend.service.sms.SmsService;
import com.example.pets_backend.service.support.IdCardGenderSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[^A-Za-z0-9].*");
    private static final int PET_OWNER_ROLE = 1;
    private static final int CARETAKER_ROLE = 2;
    private static final int BOTH_ROLE = 3;
    private static final int SMS_CODE_LENGTH = 6;
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    private static final int SMS_RATE_LIMIT_SECONDS = 60;
    private static final int ONGOING_ORDER_STATUS_MIN = 1;
    private static final int ONGOING_ORDER_STATUS_MAX = 4;

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsVerificationCodeDao smsVerificationCodeDao;
    private final SmsService smsService;
    private final OrderDao orderDao;
    private final ArbitrationRecordDao arbitrationRecordDao;
    private final SitterProfileDao sitterProfileDao;
    private final ObjectStorageService objectStorageService;

    public LoginUserRespDTO login(LoginUserReqDTO reqDTO) {
        validateLoginRequest(reqDTO);

        UserDO user = userDao.selectByPhone(reqDTO.phone());
        if (user == null) {
            throw new ClientException(BaseErrorCode.USER_LOGIN_ERROR);
        }
        if (!hasPassword(user)) {
            throw new ClientException(BaseErrorCode.PASSWORD_NOT_SET_ERROR);
        }

        String passwordHash = passwordEncoder.encrypt(reqDTO.password());
        if (!passwordHash.equals(user.getPasswordHash())) {
            throw new ClientException(BaseErrorCode.USER_LOGIN_ERROR);
        }

        return buildLoginResponse(user);
    }

    @Transactional
    public void setPassword(SetPasswordReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validatePassword(reqDTO.newPassword());
        UserDO user = requireCurrentUser();
        if (hasPassword(user)) {
            throw new ClientException(BaseErrorCode.PASSWORD_ALREADY_SET_ERROR);
        }
        user.setPasswordHash(passwordEncoder.encrypt(reqDTO.newPassword()));
        userDao.updateById(user);
    }

    @Transactional
    public void changePassword(ChangePasswordReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validatePassword(reqDTO.newPassword());
        if (reqDTO.oldPassword() == null || reqDTO.oldPassword().isBlank()) {
            throw new ClientException(BaseErrorCode.PASSWORD_OLD_ERROR);
        }
        UserDO user = requireCurrentUser();
        if (!hasPassword(user)) {
            throw new ClientException(BaseErrorCode.PASSWORD_NOT_SET_ERROR);
        }
        String oldPasswordHash = passwordEncoder.encrypt(reqDTO.oldPassword());
        if (!oldPasswordHash.equals(user.getPasswordHash())) {
            throw new ClientException(BaseErrorCode.PASSWORD_OLD_ERROR);
        }
        user.setPasswordHash(passwordEncoder.encrypt(reqDTO.newPassword()));
        userDao.updateById(user);
    }

    @Transactional
    public RegisterUserRespDTO register(RegisterUserReqDTO reqDTO) {
        validateRegisterRequest(reqDTO);

        UserDO existingUser = userDao.selectByPhone(reqDTO.phone());
        if (existingUser != null) {
            throw new ClientException(BaseErrorCode.PHONE_EXIST_ERROR);
        }

        UserDO user = new UserDO();
        user.setPhone(reqDTO.phone());
        user.setNickname(reqDTO.nickname());
        user.setPasswordHash(passwordEncoder.encrypt(reqDTO.password()));
        user.setRoleType(PET_OWNER_ROLE);
        user.setBalance(BigDecimal.ZERO);
        user.setFrozenAmount(BigDecimal.ZERO);

        userDao.insert(user);
        return toRegisterResponse(user);
    }

    public void sendVerificationCode(String phone) {
        if (phone == null || !MAINLAND_PHONE_PATTERN.matcher(phone).matches()) {
            throw new ClientException(BaseErrorCode.PHONE_VERIFY_ERROR);
        }
        if (smsVerificationCodeDao.existsByPhoneWithinSeconds(phone, SMS_RATE_LIMIT_SECONDS)) {
            throw new ClientException(BaseErrorCode.SMS_RATE_LIMIT_ERROR);
        }
        String code = generateCode();
        smsService.sendCode(phone, code);

        SmsVerificationCodeDO record = new SmsVerificationCodeDO();
        record.setPhone(phone);
        record.setCode(code);
        record.setUsed(0);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(SMS_CODE_EXPIRE_MINUTES));
        smsVerificationCodeDao.insert(record);
    }

    @Transactional
    public LoginUserRespDTO loginByCode(LoginByCodeReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.phone() == null || !MAINLAND_PHONE_PATTERN.matcher(reqDTO.phone()).matches()) {
            throw new ClientException(BaseErrorCode.PHONE_VERIFY_ERROR);
        }
        if (reqDTO.code() == null || reqDTO.code().isBlank()) {
            throw new ClientException(BaseErrorCode.SMS_CODE_INVALID_ERROR);
        }

        SmsVerificationCodeDO record = smsVerificationCodeDao.selectLatestValid(reqDTO.phone());
        if (record == null) {
            throw new ClientException(BaseErrorCode.SMS_CODE_EXPIRED_ERROR);
        }
        if (!record.getCode().equals(reqDTO.code())) {
            throw new ClientException(BaseErrorCode.SMS_CODE_INVALID_ERROR);
        }

        smsVerificationCodeDao.markUsed(record.getId());

        UserDO user = userDao.selectByPhone(reqDTO.phone());
        if (user == null) {
            user = new UserDO();
            user.setPhone(reqDTO.phone());
            user.setNickname("用户" + reqDTO.phone().substring(7));
            user.setRoleType(PET_OWNER_ROLE);
            user.setBalance(BigDecimal.ZERO);
            user.setFrozenAmount(BigDecimal.ZERO);
            userDao.insert(user);
        }

        return buildLoginResponse(user);
    }

    @Transactional
    public String applyCaretaker(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Integer roleType = user.getRoleType();
        if (roleType != null && (roleType == CARETAKER_ROLE || roleType == BOTH_ROLE)) {
            // 已经是宠托师，直接签发当前角色的 token
            return jwtUtil.generateAccessToken(new UserInfoDTO(
                    user.getUserId(), user.getPhone(), user.getNickname(), user.getRoleType(), null));
        }
        user.setRoleType(roleType == null ? CARETAKER_ROLE : BOTH_ROLE);
        userDao.updateById(user);

        SitterProfileDO profile = sitterProfileDao.selectById(userId);
        if (profile == null) {
            profile = new SitterProfileDO();
            profile.setProviderId(userId);
            profile.setVerifyStatus(0);
            profile.setDepositAmount(java.math.BigDecimal.ZERO);
            profile.setCreditScore(80);
            profile.setIsBanned(0);
            profile.setServiceRadiusKm(5);
            sitterProfileDao.insert(profile);
        }

        // 颁发包含新 roleType 的 token，前端无需重新登录
        return jwtUtil.generateAccessToken(new UserInfoDTO(
                user.getUserId(), user.getPhone(), user.getNickname(), user.getRoleType(), null));
    }

    public void deactivateAccount(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        long ongoingOrders = orderDao.countOngoingByUserId(userId,
                ONGOING_ORDER_STATUS_MIN, ONGOING_ORDER_STATUS_MAX);
        if (ongoingOrders > 0) {
            throw new ClientException(BaseErrorCode.DEACTIVATE_HAS_ONGOING_ORDER);
        }

        if (arbitrationRecordDao.existsActiveByParticipant(userId)) {
            throw new ClientException(BaseErrorCode.DEACTIVATE_HAS_ACTIVE_DISPUTE);
        }

        BigDecimal balance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        BigDecimal frozen = user.getFrozenAmount() != null ? user.getFrozenAmount() : BigDecimal.ZERO;
        if (balance.compareTo(BigDecimal.ZERO) != 0 || frozen.compareTo(BigDecimal.ZERO) != 0) {
            throw new ClientException(BaseErrorCode.DEACTIVATE_HAS_BALANCE);
        }

        user.setDeleted(1);
        userDao.updateById(user);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private LoginUserRespDTO buildLoginResponse(UserDO user) {
        String token = jwtUtil.generateAccessToken(new UserInfoDTO(
                user.getUserId(), user.getPhone(), user.getNickname(), user.getRoleType(), null));
        return new LoginUserRespDTO(
                user.getUserId(), user.getNickname(), user.getPhone(), user.getRoleType(),
                UserRoleTypeEnum.getDescByCode(user.getRoleType()), token);
    }

    private boolean hasPassword(UserDO user) {
        return user != null && StringUtils.hasText(user.getPasswordHash());
    }

    private UserDO requireCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return user;
    }

    private void validateRegisterRequest(RegisterUserReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.USER_REGISTER_ERROR);
        }
        if (reqDTO.nickname() == null || reqDTO.nickname().isBlank()) {
            throw new ClientException(BaseErrorCode.USER_NAME_VERIFY_ERROR);
        }
        if (reqDTO.phone() == null || !MAINLAND_PHONE_PATTERN.matcher(reqDTO.phone()).matches()) {
            throw new ClientException(BaseErrorCode.PHONE_VERIFY_ERROR);
        }
        validatePassword(reqDTO.password());
    }

    private void validateLoginRequest(LoginUserReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.USER_LOGIN_ERROR);
        }
        if (reqDTO.phone() == null || !MAINLAND_PHONE_PATTERN.matcher(reqDTO.phone()).matches()) {
            throw new ClientException(BaseErrorCode.PHONE_VERIFY_ERROR);
        }
        validatePassword(reqDTO.password());
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ClientException(BaseErrorCode.PASSWORD_SHORT_ERROR);
        }
        int categoryCount = 0;
        if (LETTER_PATTERN.matcher(password).matches()) categoryCount++;
        if (DIGIT_PATTERN.matcher(password).matches()) categoryCount++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) categoryCount++;
        if (categoryCount < 2) {
            throw new ClientException(BaseErrorCode.PASSWORD_VERIFY_ERROR);
        }
    }

    private RegisterUserRespDTO toRegisterResponse(UserDO user) {
        return new RegisterUserRespDTO(user.getNickname(), user.getPhone());
    }

    @Transactional
    public void realNameVerify(Long userId, String realName, String idCardNo,
                               MultipartFile frontImage, MultipartFile backImage) {
        if (realName == null || realName.isBlank() || realName.trim().length() < 2) {
            throw new ClientException(BaseErrorCode.REAL_NAME_VERIFY_ERROR);
        }
        String normalizedIdCard = idCardNo == null ? "" : idCardNo.trim().toUpperCase();
        if (!ID_CARD_PATTERN.matcher(normalizedIdCard).matches()) {
            throw new ClientException(BaseErrorCode.REAL_NAME_VERIFY_ERROR);
        }

        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        // 上传身份证原图至 OSS（仅作留存，不持久化 URL）
        if (frontImage != null && !frontImage.isEmpty()) {
            uploadIdCardImage(frontImage, userId, "front");
        }
        if (backImage != null && !backImage.isEmpty()) {
            uploadIdCardImage(backImage, userId, "back");
        }

        user.setRealName(realName.trim());
        user.setIdCardNo(normalizedIdCard);
        userDao.updateById(user);
        syncCaretakerGenderFromIdCard(user, normalizedIdCard);
    }

    /**
     * 宠托师实名认证时，根据身份证号末位写入 sitter_profiles.gender。
     */
    private void syncCaretakerGenderFromIdCard(UserDO user, String idCardNo) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        Integer roleType = user.getRoleType();
        if (roleType == null || (roleType != CARETAKER_ROLE && roleType != BOTH_ROLE)) {
            return;
        }
        Integer gender = IdCardGenderSupport.resolveGender(idCardNo);
        if (gender == null) {
            return;
        }
        SitterProfileDO profile = sitterProfileDao.selectById(user.getUserId());
        if (profile == null) {
            profile = new SitterProfileDO();
            profile.setProviderId(user.getUserId());
            profile.setVerifyStatus(0);
            profile.setDepositAmount(BigDecimal.ZERO);
            profile.setCreditScore(80);
            profile.setIsBanned(0);
            profile.setServiceRadiusKm(5);
            profile.setGender(gender);
            sitterProfileDao.insert(profile);
            return;
        }
        profile.setGender(gender);
        sitterProfileDao.updateById(profile);
    }

    private void uploadIdCardImage(MultipartFile file, Long userId, String side) {
        String objectKey = String.format("id-card/%d/%s_%s", userId, side, UUID.randomUUID());
        try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            objectStorageService.upload(
                    new OssUploadCommand(objectKey, inputStream, file.getSize(), file.getContentType()));
        } catch (IOException e) {
            // 图片存储失败不阻断认证流程，仅记录日志
        }
    }
}
