package com.example.pets_backend.service;

import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.ReviewDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.ReviewDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.CaretakerProfileUpdateReqDTO;
import com.example.pets_backend.dto.req.UpdateCaretakerAvailabilityReqDTO;
import com.example.pets_backend.dto.resp.CaretakerAvailabilityRespDTO;
import com.example.pets_backend.dto.resp.CaretakerProfileRespDTO;
import com.example.pets_backend.dto.resp.CaretakerStatsRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CaretakerProfileService {

    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int ORDER_STATUS_COMPLETED = 6;
    private static final int ORDER_STATUS_PENDING_PAY = 2;
    private static final int REVIEW_TYPE_OWNER_TO_PROVIDER = 1;
    private static final int VERIFY_STATUS_APPROVED = 2;
    private static final int DEFAULT_CREDIT_SCORE = 80;
    private static final int DEFAULT_SERVICE_RANGE_KM = 5;
    private static final int NICKNAME_MAX_LENGTH = 50;
    private static final int AVATAR_URL_MAX_LENGTH = 255;
    private static final int RESIDENT_ADDRESS_MAX_LENGTH = 255;
    // 0 = 不限距离，正值表示具体公里数上限
    private static final int SERVICE_RANGE_MIN_KM = 0;
    private static final int SERVICE_RANGE_MAX_KM = 100;
    private static final double LATITUDE_MIN = -90.0;
    private static final double LATITUDE_MAX = 90.0;
    private static final double LONGITUDE_MIN = -180.0;
    private static final double LONGITUDE_MAX = 180.0;
    /** 系统维护的标签，用户不可自选写入 certLabelsJson */
    private static final Set<String> SYSTEM_MANAGED_CERT_LABELS = Set.of(
            "实名认证", "平台认证", "10+次服务", "50+次服务");

    private final UserDao userDao;
    private final OrderDao orderDao;
    private final ReviewDao reviewDao;
    private final SitterProfileDao sitterProfileDao;
    private final SitterTrainingRecordDao sitterTrainingRecordDao;
    private final ObjectMapper objectMapper;
    private final OssAccessibleUrlService ossAccessibleUrlService;
    private final ConcurrentMap<Long, Boolean> availabilityStateCache = new ConcurrentHashMap<>();

    public CaretakerProfileRespDTO getMyProfile() {
        Long currentUserId = currentUserId();
        requireCaretakerRole();
        UserDO user = userDao.selectById(currentUserId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        SitterProfileDO profile = sitterProfileDao.selectById(currentUserId);
        SitterTrainingRecordDO trainingRecord = sitterTrainingRecordDao.selectById(currentUserId);

        int creditScore = profile == null || profile.getCreditScore() == null
                ? DEFAULT_CREDIT_SCORE
                : profile.getCreditScore();
        int reviewCount = Math.toIntExact(reviewDao.countByTargetIdAndType(currentUserId, REVIEW_TYPE_OWNER_TO_PROVIDER));
        int completedCount = Math.toIntExact(orderDao.countByProviderIdAndStatus(currentUserId, ORDER_STATUS_COMPLETED));

        return new CaretakerProfileRespDTO(
                user.getNickname(),
                ossAccessibleUrlService.toDisplayUrl(user.getAvatarUrl()),
                profile == null ? null : profile.getGender(),
                resolveRating(currentUserId, reviewCount, creditScore),
                resolveLevelTag(creditScore),
                resolveCertTags(user, profile),
                resolveCertLabels(profile, trainingRecord, completedCount),
                resolveServiceRangeKm(profile),
                profile == null ? null : profile.getResidentAddress(),
                reviewCount);
    }

    @Transactional
    public CaretakerProfileRespDTO updateMyProfile(CaretakerProfileUpdateReqDTO reqDTO) {
        Long currentUserId = currentUserId();
        requireCaretakerRole();
        validateUpdateRequest(reqDTO);

        UserDO user = userDao.selectById(currentUserId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        user.setNickname(reqDTO.nickname().trim());
        // avatarUrl 为空时保留现有头像，不覆盖；入库统一存 OSS objectKey
        if (StringUtils.hasText(reqDTO.avatarUrl())) {
            user.setAvatarUrl(ossAccessibleUrlService.normalizeForStorage(reqDTO.avatarUrl()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userDao.updateById(user);

        SitterProfileDO profile = ensureProfile(currentUserId);
        profile.setServiceRadiusKm(reqDTO.serviceRangeKm());
        profile.setResidentAddress(normalizeText(reqDTO.residentAddress()));
        profile.setResidentLatitude(toBigDecimal(reqDTO.residentLatitude()));
        profile.setResidentLongitude(toBigDecimal(reqDTO.residentLongitude()));
        profile.setCertLabelsJson(serializeCertLabels(filterUserSelectableCertLabels(reqDTO.certLabels())));
        profile.setUpdatedAt(LocalDateTime.now());
        sitterProfileDao.updateById(profile);

        return getMyProfile();
    }

    public CaretakerStatsRespDTO getMyStats() {
        Long currentUserId = currentUserId();
        requireCaretakerRole();

        SitterProfileDO profile = sitterProfileDao.selectById(currentUserId);
        int creditScore = profile == null || profile.getCreditScore() == null
                ? DEFAULT_CREDIT_SCORE
                : profile.getCreditScore();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);
        int todayOrderCount = Math.toIntExact(
                orderDao.countByProviderIdAndCreatedBetween(currentUserId, startOfDay, endOfDay));
        int pendingPaymentCount = Math.toIntExact(
                orderDao.countByProviderIdAndStatus(currentUserId, ORDER_STATUS_PENDING_PAY));

        return new CaretakerStatsRespDTO(todayOrderCount, creditScore, pendingPaymentCount);
    }

    public CaretakerAvailabilityRespDTO getMyAvailability() {
        Long currentUserId = currentUserId();
        requireCaretakerRole();
        return new CaretakerAvailabilityRespDTO(
                availabilityStateCache.getOrDefault(currentUserId, true));
    }

    @Transactional
    public CaretakerAvailabilityRespDTO updateMyAvailability(UpdateCaretakerAvailabilityReqDTO reqDTO) {
        Long currentUserId = currentUserId();
        requireCaretakerRole();
        if (reqDTO == null || reqDTO.isAvailable() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        boolean isAvailable = Boolean.TRUE.equals(reqDTO.isAvailable());
        availabilityStateCache.put(currentUserId, isAvailable);
        return new CaretakerAvailabilityRespDTO(isAvailable);
    }

    private int resolveServiceRangeKm(SitterProfileDO profile) {
        if (profile == null || profile.getServiceRadiusKm() == null) {
            return DEFAULT_SERVICE_RANGE_KM;
        }
        return profile.getServiceRadiusKm();
    }

    private double resolveRating(Long providerId, int reviewCount, int creditScore) {
        if (reviewCount > 0) {
            List<ReviewDO> reviews = reviewDao.selectByTargetIdAndType(providerId, REVIEW_TYPE_OWNER_TO_PROVIDER);
            double sum = reviews.stream()
                    .mapToDouble(review -> mapReviewScoreToStars(
                            review.getOverallScore() == null ? review.getScore() : review.getOverallScore()))
                    .sum();
            return roundToOneDecimal(sum / reviewCount);
        }
        return roundToOneDecimal(Math.min(5.0, creditScore / 20.0));
    }

    private double mapReviewScoreToStars(Integer score) {
        if (score == null) {
            return 5.0;
        }
        if (score >= 2 && score <= 5) {
            return score;
        }
        return switch (score) {
            case 1 -> 5.0;
            case 6 -> 1.0;
            default -> 5.0;
        };
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String resolveLevelTag(int creditScore) {
        if (creditScore >= 95) {
            return "金牌宠托师";
        }
        if (creditScore >= 85) {
            return "银牌宠托师";
        }
        if (creditScore >= 70) {
            return "铜牌宠托师";
        }
        return "新手宠托师";
    }

    private List<String> resolveCertTags(UserDO user, SitterProfileDO profile) {
        List<String> certTags = new ArrayList<>();
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            certTags.add("实名认证");
        }
        return certTags;
    }

    /**
     * 合并用户自选标签（存储在 certLabelsJson）与系统自动生成的标签（平台认证、N+次服务）。
     * 用 LinkedHashSet 保持顺序并去重：用户选的在前，系统生成的在后。
     */
    private List<String> resolveCertLabels(SitterProfileDO profile,
            SitterTrainingRecordDO trainingRecord, int completedCount) {
        LinkedHashSet<String> labels = new LinkedHashSet<>(deserializeCertLabels(profile));
        if (isPlatformCertified(profile, trainingRecord)) {
            labels.add("平台认证");
        }
        if (completedCount >= 50) {
            labels.add("50+次服务");
        } else if (completedCount >= 10) {
            labels.add("10+次服务");
        }
        return new ArrayList<>(labels);
    }

    private boolean isPlatformCertified(SitterProfileDO profile, SitterTrainingRecordDO trainingRecord) {
        if (profile != null && Objects.equals(profile.getVerifyStatus(), VERIFY_STATUS_APPROVED)) {
            return true;
        }
        return trainingRecord != null && Objects.equals(trainingRecord.getLastExamPassed(), 1);
    }

    private List<String> filterUserSelectableCertLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> filtered = new LinkedHashSet<>();
        for (String label : labels) {
            if (!StringUtils.hasText(label)) {
                continue;
            }
            String normalized = label.trim();
            if (!SYSTEM_MANAGED_CERT_LABELS.contains(normalized)) {
                filtered.add(normalized);
            }
        }
        return new ArrayList<>(filtered);
    }

    private String serializeCertLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(labels);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> deserializeCertLabels(SitterProfileDO profile) {
        if (profile == null || !StringUtils.hasText(profile.getCertLabelsJson())) {
            return List.of();
        }
        try {
            List<String> stored = objectMapper.readValue(profile.getCertLabelsJson(),
                    new TypeReference<List<String>>() {});
            return filterUserSelectableCertLabels(stored);
        } catch (Exception e) {
            return List.of();
        }
    }

    private void validateUpdateRequest(CaretakerProfileUpdateReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validateRequiredText(reqDTO.nickname(), NICKNAME_MAX_LENGTH);
        // avatarUrl 可为空（用户未设头像时保留现有值），有值时才校验长度（按规范化后的 objectKey 校验）
        if (StringUtils.hasText(reqDTO.avatarUrl())) {
            String normalized = ossAccessibleUrlService.normalizeForStorage(reqDTO.avatarUrl());
            if (normalized.length() > AVATAR_URL_MAX_LENGTH) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
        }
        // serviceRangeKm: 0 = 不限距离，正值为具体公里数，均合法
        if (reqDTO.serviceRangeKm() == null
                || reqDTO.serviceRangeKm() < SERVICE_RANGE_MIN_KM
                || reqDTO.serviceRangeKm() > SERVICE_RANGE_MAX_KM) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO.residentAddress() != null && reqDTO.residentAddress().trim().length() > RESIDENT_ADDRESS_MAX_LENGTH) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validateCoordinates(reqDTO.residentLatitude(), reqDTO.residentLongitude());
    }

    private void validateRequiredText(String text, int maxLength) {
        if (!StringUtils.hasText(text) || text.trim().length() > maxLength) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX
                || longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private SitterProfileDO ensureProfile(Long providerId) {
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        if (profile == null) {
            profile = new SitterProfileDO();
            profile.setProviderId(providerId);
            profile.setVerifyStatus(0);
            profile.setDepositAmount(BigDecimal.ZERO);
            profile.setCreditScore(DEFAULT_CREDIT_SCORE);
            profile.setIsBanned(0);
            profile.setServiceRadiusKm(DEFAULT_SERVICE_RANGE_KM);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            profile.setDeleted(0);
            sitterProfileDao.insert(profile);
        }
        return profile;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private void requireCaretakerRole() {
        Integer roleType = UserContext.getRoleType();
        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR);
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}
