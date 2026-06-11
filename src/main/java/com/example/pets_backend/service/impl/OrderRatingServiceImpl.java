package com.example.pets_backend.service.impl;

import com.example.pets_backend.config.ReviewRatingProperties;
import com.example.pets_backend.dao.CreditRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.ReviewAppealDao;
import com.example.pets_backend.dao.ReviewAttachmentDao;
import com.example.pets_backend.dao.ReviewDeductionReasonDao;
import com.example.pets_backend.dao.ReviewDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.CreditRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.ReviewAttachmentDO;
import com.example.pets_backend.dao.entity.ReviewDeductionReasonDO;
import com.example.pets_backend.dao.entity.ReviewDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dto.req.ReviewAttachmentReqDTO;
import com.example.pets_backend.dto.req.ReviewDeductionReasonReqDTO;
import com.example.pets_backend.dto.req.SubmitRatingReqDTO;
import com.example.pets_backend.dto.resp.OrderRatingDetailRespDTO;
import com.example.pets_backend.dto.resp.ReviewDeductionReasonRespDTO;
import com.example.pets_backend.enums.CreditActionTypeEnum;
import com.example.pets_backend.enums.TrainingResetReasonEnum;
import com.example.pets_backend.enums.ReviewDeductionReasonEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.OrderRatingService;
import com.example.pets_backend.service.TrainingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderRatingServiceImpl implements OrderRatingService {

    private final OrderDao orderDao;
    private final ReviewDao reviewDao;
    private final ReviewAppealDao reviewAppealDao;
    private final ReviewAttachmentDao reviewAttachmentDao;
    private final ReviewDeductionReasonDao reviewDeductionReasonDao;
    private final SitterProfileDao sitterProfileDao;
    private final CreditRecordDao creditRecordDao;
    private final ReviewRatingProperties reviewRatingProperties;
    private final TrainingService trainingService;

    private static final int ORDER_STATUS_COMPLETED = 6;
    private static final int REVIEW_TYPE_OWNER_TO_PROVIDER = 1;
    private static final int REVIEW_STATUS_NORMAL = 1;
    private static final int REVIEW_STATUS_APPEALING = 2;
    private static final int LOW_SCORE_YES = 1;
    private static final int LOW_SCORE_NO = 0;
    private static final int MIN_CREDIT_SCORE = 0;
    private static final int MAX_CREDIT_SCORE = 100;
    private static final int DEFAULT_CREDIT_SCORE = 80;
    private static final int MAX_ATTACHMENT_COUNT = 6;
    private static final int APPEAL_WINDOW_DAYS = 7;

    @Override
    @Transactional
    public void submitRating(Long orderId, SubmitRatingReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        RatingScores scores = resolveScores(reqDTO);
        boolean lowScore = isLowScore(scores);
        List<ReviewDeductionReasonEnum> deductionReasons = resolveDeductionReasons(reqDTO);
        validateRatingRequest(reqDTO, lowScore, deductionReasons);

        Long currentUser = UserContext.getUserId();
        if (currentUser == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }

        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        if (!currentUser.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (order.getStatus() == null || ORDER_STATUS_COMPLETED != order.getStatus()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        boolean already = reviewDao.existsByOrderIdAndReviewerAndType(orderId, currentUser, REVIEW_TYPE_OWNER_TO_PROVIDER);
        if (already) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        ReviewDO review = new ReviewDO();
        review.setOrderId(orderId);
        review.setReviewerId(currentUser);
        review.setTargetId(order.getProviderId());
        review.setScore(scores.overallScore());
        review.setOverallScore(scores.overallScore());
        review.setPunctualityScore(scores.punctualityScore());
        review.setProfessionalScore(scores.professionalScore());
        review.setIsLowScore(lowScore ? LOW_SCORE_YES : LOW_SCORE_NO);
        review.setReviewStatus(REVIEW_STATUS_NORMAL);
        review.setContent(normalizeComment(reqDTO.getComment()));
        review.setReviewType(REVIEW_TYPE_OWNER_TO_PROVIDER);
        reviewDao.insert(review);
        reviewDeductionReasonDao.insertBatch(buildDeductionReasonDOs(review.getReviewId(), reqDTO.getDeductionReasons()));
        reviewAttachmentDao.insertBatch(buildAttachmentDOs(review.getReviewId(), reqDTO.getAttachments()));

        Long providerId = order.getProviderId();
        if (providerId != null) {
            CreditActionTypeEnum actionType = resolveCreditActionType(scores, deductionReasons);
            if (actionType != null) {
                applyCreditAction(providerId, orderId, actionType);
                if (shouldResetTraining(actionType)) {
                    trainingService.resetTraining(
                            providerId, TrainingResetReasonEnum.COMPLAINT_OPERATION.getCode());
                }
            }
        }
    }

    @Override
    public OrderRatingDetailRespDTO getRating(Long orderId) {
        Long currentUser = UserContext.getUserId();
        if (currentUser == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }

        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        if (!currentUser.equals(order.getOwnerId()) && !currentUser.equals(order.getProviderId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }

        ReviewDO review = reviewDao.selectByOrderIdAndType(orderId, REVIEW_TYPE_OWNER_TO_PROVIDER);
        if (review == null) {
            return null;
        }

        Long reviewId = review.getReviewId();
        List<ReviewDeductionReasonDO> reasons = reviewDeductionReasonDao.selectByReviewIds(List.of(reviewId));
        List<ReviewAttachmentDO> attachments = reviewAttachmentDao.selectByReviewIds(List.of(reviewId));

        return new OrderRatingDetailRespDTO(
                review.getReviewId(),
                review.getOrderId(),
                resolveOverallScore(review),
                review.getPunctualityScore(),
                review.getProfessionalScore(),
                review.getContent(),
                toReasonResp(reasons),
                toAttachmentResp(attachments),
                review.getReviewStatus(),
                reviewStatusDesc(review.getReviewStatus()),
                canAppeal(review),
                appealDeadline(review),
                review.getCreatedAt());
    }

    private Integer resolveOverallScore(ReviewDO review) {
        return review.getOverallScore() == null ? review.getScore() : review.getOverallScore();
    }

    private List<ReviewDeductionReasonRespDTO> toReasonResp(List<ReviewDeductionReasonDO> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return List.of();
        }
        return reasons.stream()
                .map(reason -> new ReviewDeductionReasonRespDTO(
                        reason.getReasonType(),
                        reasonTypeDesc(reason.getReasonType()),
                        resolveReasonText(reason)))
                .toList();
    }

    private String resolveReasonText(ReviewDeductionReasonDO reason) {
        if (!isBlank(reason.getReasonText())) {
            return reason.getReasonText();
        }
        ReviewDeductionReasonEnum reasonEnum = ReviewDeductionReasonEnum.fromCode(reason.getReasonType());
        return reasonEnum == null ? null : reasonEnum.getDesc();
    }

    private String reasonTypeDesc(Integer reasonType) {
        ReviewDeductionReasonEnum reasonEnum = ReviewDeductionReasonEnum.fromCode(reasonType);
        return reasonEnum == null ? null : reasonEnum.getDesc();
    }

    private List<OrderRatingDetailRespDTO.Attachment> toAttachmentResp(List<ReviewAttachmentDO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .map(attachment -> new OrderRatingDetailRespDTO.Attachment(
                        attachment.getUrl(),
                        attachment.getObjectKey(),
                        attachment.getMediaType(),
                        attachment.getContentType(),
                        attachment.getFileSize(),
                        attachment.getSortOrder()))
                .toList();
    }

    private Boolean canAppeal(ReviewDO review) {
        Long currentUser = UserContext.getUserId();
        LocalDateTime deadline = appealDeadline(review);
        return Objects.equals(review.getTargetId(), currentUser)
                && LOW_SCORE_YES == safeInt(review.getIsLowScore())
                && reviewDeductionReasonDao.existsByReviewId(review.getReviewId())
                && (deadline == null || !LocalDateTime.now().isAfter(deadline))
                && REVIEW_STATUS_NORMAL == safeInt(review.getReviewStatus())
                && !reviewAppealDao.existsActiveByReviewId(review.getReviewId());
    }

    private LocalDateTime appealDeadline(ReviewDO review) {
        return review.getCreatedAt() == null ? null : review.getCreatedAt().plusDays(APPEAL_WINDOW_DAYS);
    }

    private String reviewStatusDesc(Integer reviewStatus) {
        return switch (safeInt(reviewStatus)) {
            case 1 -> "正常";
            case 2 -> "申诉中";
            case 3 -> "申诉成立";
            case 4 -> "申诉驳回";
            case 5 -> "已隐藏";
            case 6 -> "已修正";
            default -> null;
        };
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private RatingScores resolveScores(SubmitRatingReqDTO reqDTO) {
        Integer overallScore = reqDTO.getOverallScore() != null ? reqDTO.getOverallScore() : reqDTO.getScore();
        if (overallScore == null && CreditActionTypeEnum.FIVE_STAR.getCode().equals(reqDTO.getActionType())) {
            overallScore = 5;
        }
        Integer punctualityScore = reqDTO.getPunctualityScore() == null ? overallScore : reqDTO.getPunctualityScore();
        Integer professionalScore = reqDTO.getProfessionalScore() == null ? overallScore : reqDTO.getProfessionalScore();
        if (!isValidScore(overallScore) || !isValidScore(punctualityScore) || !isValidScore(professionalScore)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return new RatingScores(overallScore, punctualityScore, professionalScore);
    }

    private boolean isLowScore(RatingScores scores) {
        int threshold = reviewRatingProperties.getLowScoreThreshold();
        List<String> dimensions = reviewRatingProperties.getLowScoreDimensions();
        if (dimensions == null || dimensions.isEmpty()) {
            dimensions = List.of("overall", "punctuality", "professional");
        }
        for (String dimension : dimensions) {
            Integer score = scoreByDimension(scores, dimension);
            if (score != null && score <= threshold) {
                return true;
            }
        }
        return false;
    }

    private Integer scoreByDimension(RatingScores scores, String dimension) {
        if (dimension == null) {
            return null;
        }
        return switch (dimension.trim().toLowerCase()) {
            case "overall" -> scores.overallScore();
            case "punctuality" -> scores.punctualityScore();
            case "professional" -> scores.professionalScore();
            default -> null;
        };
    }

    private List<ReviewDeductionReasonEnum> resolveDeductionReasons(SubmitRatingReqDTO reqDTO) {
        if (reqDTO.getDeductionReasons() == null || reqDTO.getDeductionReasons().isEmpty()) {
            return List.of();
        }
        Set<Integer> seenReasonTypes = new HashSet<>();
        List<ReviewDeductionReasonEnum> result = new ArrayList<>();
        for (ReviewDeductionReasonReqDTO reasonReqDTO : reqDTO.getDeductionReasons()) {
            if (reasonReqDTO == null || reasonReqDTO.getReasonType() == null
                    || !seenReasonTypes.add(reasonReqDTO.getReasonType())) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            ReviewDeductionReasonEnum reason = ReviewDeductionReasonEnum.fromCode(reasonReqDTO.getReasonType());
            if (reason == null) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            if (reason == ReviewDeductionReasonEnum.OTHER && isBlank(reasonReqDTO.getReasonText())) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            result.add(reason);
        }
        return result;
    }

    private void validateRatingRequest(SubmitRatingReqDTO reqDTO, boolean lowScore,
            List<ReviewDeductionReasonEnum> deductionReasons) {
        if (lowScore) {
            if (isBlank(reqDTO.getComment()) || deductionReasons.isEmpty()) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            return;
        }
        if (!deductionReasons.isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private List<ReviewDeductionReasonDO> buildDeductionReasonDOs(Long reviewId,
            List<ReviewDeductionReasonReqDTO> reasonReqDTOs) {
        if (reasonReqDTOs == null || reasonReqDTOs.isEmpty()) {
            return List.of();
        }
        return reasonReqDTOs.stream()
                .map(reasonReqDTO -> {
                    ReviewDeductionReasonDO reason = new ReviewDeductionReasonDO();
                    reason.setReviewId(reviewId);
                    reason.setReasonType(reasonReqDTO.getReasonType());
                    reason.setReasonText(normalizeComment(reasonReqDTO.getReasonText()));
                    return reason;
                })
                .toList();
    }

    private List<ReviewAttachmentDO> buildAttachmentDOs(Long reviewId, List<ReviewAttachmentReqDTO> attachmentReqDTOs) {
        if (attachmentReqDTOs == null || attachmentReqDTOs.isEmpty()) {
            return List.of();
        }
        if (attachmentReqDTOs.size() > MAX_ATTACHMENT_COUNT) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        List<ReviewAttachmentDO> attachments = new ArrayList<>();
        for (int i = 0; i < attachmentReqDTOs.size(); i++) {
            ReviewAttachmentReqDTO reqDTO = attachmentReqDTOs.get(i);
            if (reqDTO == null || isBlank(reqDTO.getUrl()) || isBlank(reqDTO.getMediaType())) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            ReviewAttachmentDO attachment = new ReviewAttachmentDO();
            attachment.setReviewId(reviewId);
            attachment.setUrl(reqDTO.getUrl().trim());
            attachment.setObjectKey(normalizeComment(reqDTO.getObjectKey()));
            attachment.setMediaType(reqDTO.getMediaType().trim());
            attachment.setContentType(normalizeComment(reqDTO.getContentType()));
            attachment.setFileSize(reqDTO.getFileSize());
            attachment.setSortOrder(reqDTO.getSortOrder() == null ? i + 1 : reqDTO.getSortOrder());
            attachments.add(attachment);
        }
        return attachments;
    }

    private CreditActionTypeEnum resolveCreditActionType(RatingScores scores,
            List<ReviewDeductionReasonEnum> deductionReasons) {
        if (!deductionReasons.isEmpty()) {
            return deductionReasons.stream()
                    .map(ReviewDeductionReasonEnum::getCreditActionType)
                    .min(Comparator.comparingInt(CreditActionTypeEnum::getDelta))
                    .orElse(null);
        }
        return scores.overallScore() == 5 ? CreditActionTypeEnum.FIVE_STAR : null;
    }

    private boolean isValidScore(Integer score) {
        return score != null && score >= 1 && score <= 5;
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void applyCreditAction(Long providerId, Long orderId, CreditActionTypeEnum actionType) {
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        if (profile == null) {
            profile = new SitterProfileDO();
            profile.setProviderId(providerId);
            profile.setCreditScore(DEFAULT_CREDIT_SCORE);
            profile.setVerifyStatus(2);
            profile.setDepositAmount(BigDecimal.ZERO);
            profile.setIsBanned(0);
            sitterProfileDao.insert(profile);
        }

        int currentScore = profile.getCreditScore() == null ? DEFAULT_CREDIT_SCORE : profile.getCreditScore();
        int newScore;
        if (actionType == CreditActionTypeEnum.SERIOUS_ACCIDENT) {
            newScore = MIN_CREDIT_SCORE;
            profile.setIsBanned(1);
            profile.setVerifyStatus(0);
        } else {
            newScore = clamp(currentScore + actionType.getDelta(), MIN_CREDIT_SCORE, MAX_CREDIT_SCORE);
            if (profile.getIsBanned() == null) {
                profile.setIsBanned(0);
            }
        }
        profile.setCreditScore(newScore);
        sitterProfileDao.updateById(profile);

        CreditRecordDO record = new CreditRecordDO();
        record.setProviderId(providerId);
        record.setChangeScore(newScore - currentScore);
        record.setScoreAfter(newScore);
        record.setReasonType(actionType.getCode());
        record.setRelationId(orderId);
        creditRecordDao.insert(record);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean shouldResetTraining(CreditActionTypeEnum actionType) {
        return actionType == CreditActionTypeEnum.NOT_ON_TIME
                || actionType == CreditActionTypeEnum.LOW_SERVICE_QUALITY
                || actionType == CreditActionTypeEnum.BAD_ATTITUDE
                || actionType == CreditActionTypeEnum.LATE_CHECKIN
                || actionType == CreditActionTypeEnum.SERIOUS_ACCIDENT;
    }

    private record RatingScores(Integer overallScore, Integer punctualityScore, Integer professionalScore) {
    }
}
