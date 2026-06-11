package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.ReviewDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.ReviewDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.enums.OrderStatusEnum;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderProfileSupportService {

    private static final int REVIEW_TYPE_OWNER_TO_PROVIDER = 1;
    private static final int VERIFY_STATUS_APPROVED = 2;
    private static final int DEFAULT_CREDIT_SCORE = 80;
    private static final int ORDER_STATUS_COMPLETED = OrderStatusEnum.COMPLETED.getCode();
    private static final int ORDER_STATUS_BLOCKED = OrderStatusEnum.BLOCKED_WAIT_OWNER.getCode();
    private static final int ORDER_STATUS_EXCEPTION = OrderStatusEnum.EXCEPTION_ENDED.getCode();

    private final UserDao userDao;
    private final OrderDao orderDao;
    private final ReviewDao reviewDao;
    private final SitterProfileDao sitterProfileDao;
    private final SitterTrainingRecordDao sitterTrainingRecordDao;

    public ProviderPublicMetrics resolveMetrics(Long providerId) {
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        UserDO user = userDao.selectById(providerId);
        SitterTrainingRecordDO trainingRecord = sitterTrainingRecordDao.selectById(providerId);

        int creditScore = profile == null || profile.getCreditScore() == null
                ? DEFAULT_CREDIT_SCORE
                : profile.getCreditScore();
        int reviewCount = Math.toIntExact(reviewDao.countByTargetIdAndType(providerId, REVIEW_TYPE_OWNER_TO_PROVIDER));
        int totalOrderCount = Math.toIntExact(orderDao.countByProviderIdAndStatus(providerId, ORDER_STATUS_COMPLETED));
        double rating = resolveRating(providerId, reviewCount, creditScore);
        double complianceRate = resolveComplianceRate(providerId);

        return new ProviderPublicMetrics(
                creditScore,
                rating,
                reviewCount,
                totalOrderCount,
                complianceRate,
                resolveLevelTag(creditScore),
                resolveCertLabels(user, profile, trainingRecord, totalOrderCount));
    }

    public Double resolveDistanceKm(OrderAddressSnapshotDO orderAddress, SitterProfileDO profile) {
        if (orderAddress == null || profile == null) {
            return null;
        }
        Double orderLat = orderAddress.getLatitude();
        Double orderLng = orderAddress.getLongitude();
        Double providerLat = toDouble(profile.getResidentLatitude());
        Double providerLng = toDouble(profile.getResidentLongitude());
        return GeoUtils.distanceKm(orderLat, orderLng, providerLat, providerLng);
    }

    private double resolveComplianceRate(Long providerId) {
        long completedCount = orderDao.countByProviderIdAndStatus(providerId, ORDER_STATUS_COMPLETED);
        long blockedCount = orderDao.countByProviderIdAndStatus(providerId, ORDER_STATUS_BLOCKED);
        long exceptionCount = orderDao.countByProviderIdAndStatus(providerId, ORDER_STATUS_EXCEPTION);
        long denominator = completedCount + blockedCount + exceptionCount;
        if (denominator == 0) {
            return 100.0;
        }
        return GeoUtils.roundToOneDecimal(completedCount * 100.0 / denominator);
    }

    private double resolveRating(Long providerId, int reviewCount, int creditScore) {
        if (reviewCount > 0) {
            List<ReviewDO> reviews = reviewDao.selectByTargetIdAndType(providerId, REVIEW_TYPE_OWNER_TO_PROVIDER);
            double sum = reviews.stream()
                    .mapToDouble(review -> mapReviewScoreToStars(
                            review.getOverallScore() == null ? review.getScore() : review.getOverallScore()))
                    .sum();
            return GeoUtils.roundToOneDecimal(sum / reviewCount);
        }
        return GeoUtils.roundToOneDecimal(Math.min(5.0, creditScore / 20.0));
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

    private List<String> resolveCertLabels(UserDO user, SitterProfileDO profile,
            SitterTrainingRecordDO trainingRecord, int completedCount) {
        List<String> certLabels = new ArrayList<>();
        if (user != null
                && user.getRealName() != null
                && !user.getRealName().isBlank()
                && profile != null
                && Objects.equals(profile.getVerifyStatus(), VERIFY_STATUS_APPROVED)) {
            certLabels.add("实名认证");
        }
        if ((profile != null && Objects.equals(profile.getVerifyStatus(), VERIFY_STATUS_APPROVED))
                || (trainingRecord != null && Objects.equals(trainingRecord.getLastExamPassed(), 1))) {
            certLabels.add("平台认证");
        }
        if (completedCount >= 50) {
            certLabels.add("50+次服务");
        } else if (completedCount >= 10) {
            certLabels.add("10+次服务");
        }
        return certLabels;
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
