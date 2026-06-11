package com.example.pets_backend.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pets_backend.dto.resp.AdminReviewAppealPageRespDTO;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.CreditRecordDao;
import com.example.pets_backend.dao.ReviewAppealDao;
import com.example.pets_backend.dao.ReviewAttachmentDao;
import com.example.pets_backend.dao.ReviewDao;
import com.example.pets_backend.dao.ReviewDeductionReasonDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.CreditRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.ReviewAppealDO;
import com.example.pets_backend.dao.entity.ReviewAttachmentDO;
import com.example.pets_backend.dao.entity.ReviewDO;
import com.example.pets_backend.dao.entity.ReviewDeductionReasonDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dto.req.SubmitReviewAppealReqDTO;
import com.example.pets_backend.dto.req.UpdateReviewAppealStatusReqDTO;
import com.example.pets_backend.dto.req.UpdateReviewStatusReqDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewItemRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewPageRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewStatsRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealEligibilityRespDTO;
import com.example.pets_backend.dto.resp.ReviewAttachmentRespDTO;
import com.example.pets_backend.dto.resp.ReviewDeductionReasonRespDTO;
import com.example.pets_backend.dto.resp.ReviewPetBriefRespDTO;
import com.example.pets_backend.dto.resp.SubmitReviewAppealRespDTO;
import com.example.pets_backend.enums.CreditActionTypeEnum;
import com.example.pets_backend.enums.PetTypeEnum;
import com.example.pets_backend.enums.ReviewDeductionReasonEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.ReviewService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final int REVIEW_TYPE_OWNER_TO_PROVIDER = 1;
    private static final int REVIEW_STATUS_NORMAL = 1;
    private static final int REVIEW_STATUS_APPEALING = 2;
    private static final int REVIEW_STATUS_APPEAL_SUCCESS = 3;
    private static final int REVIEW_STATUS_APPEAL_REJECTED = 4;
    private static final int APPEAL_STATUS_PENDING = 1;
    private static final int APPEAL_STATUS_EVIDENCE_COLLECTING = 2;
    private static final int APPEAL_STATUS_DECIDED = 3;
    private static final int APPEAL_STATUS_SUCCESS = 4;
    private static final int APPEAL_STATUS_FAILED = 5;
    private static final int LOW_SCORE_YES = 1;
    private static final int APPEAL_WINDOW_DAYS = 7;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;

    private final ReviewDao reviewDao;
    private final ReviewDeductionReasonDao reviewDeductionReasonDao;
    private final ReviewAttachmentDao reviewAttachmentDao;
    private final ReviewAppealDao reviewAppealDao;
    private final OrderDao orderDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final CreditRecordDao creditRecordDao;
    private final SitterProfileDao sitterProfileDao;

    @Override
    public CaretakerReviewPageRespDTO listCaretakerReviews(Integer page, Integer pageSize,
            Integer reviewStatus, Boolean lowScoreOnly) {
        Long providerId = requireCurrentUser();
        requireCaretakerRole();
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        IPage<ReviewDO> reviewPage = reviewDao.selectPageByTargetIdAndType(providerId,
                REVIEW_TYPE_OWNER_TO_PROVIDER, reviewStatus, lowScoreOnly, currentPage, currentPageSize);
        List<ReviewDO> reviews = reviewPage.getRecords();
        if (reviews == null || reviews.isEmpty()) {
            return new CaretakerReviewPageRespDTO(reviewPage.getTotal(), currentPage, currentPageSize, List.of());
        }
        ReviewContext context = loadReviewContext(reviews);
        List<CaretakerReviewItemRespDTO> items = reviews.stream()
                .map(review -> buildReviewItem(review, context))
                .toList();
        return new CaretakerReviewPageRespDTO(reviewPage.getTotal(), currentPage, currentPageSize, items);
    }

    @Override
    public CaretakerReviewItemRespDTO getCaretakerReviewDetail(Long reviewId) {
        Long providerId = requireCurrentUser();
        requireCaretakerRole();
        ReviewDO review = requireReview(reviewId);
        ensureReviewBelongsToProvider(review, providerId);
        ReviewContext context = loadReviewContext(List.of(review));
        return buildReviewItem(review, context);
    }

    @Override
    public CaretakerReviewStatsRespDTO getCaretakerReviewStats() {
        Long providerId = requireCurrentUser();
        requireCaretakerRole();
        List<ReviewDO> reviews = reviewDao.selectByTargetIdAndType(providerId, REVIEW_TYPE_OWNER_TO_PROVIDER);
        long reviewCount = reviews.size();
        long lowScoreCount = reviews.stream()
                .filter(review -> LOW_SCORE_YES == safeInt(review.getIsLowScore()))
                .count();
        LocalDateTime recentStart = LocalDateTime.now().minusDays(30);
        long recent30DayReviewCount = reviewDao.countByTargetIdAndTypeAfter(providerId,
                REVIEW_TYPE_OWNER_TO_PROVIDER, recentStart, false);
        long recent30DayLowScoreCount = reviewDao.countByTargetIdAndTypeAfter(providerId,
                REVIEW_TYPE_OWNER_TO_PROVIDER, recentStart, true);
        return new CaretakerReviewStatsRespDTO(
                reviewCount,
                avg(reviews, ReviewDO::getOverallScore),
                avg(reviews, ReviewDO::getPunctualityScore),
                avg(reviews, ReviewDO::getProfessionalScore),
                lowScoreCount,
                rate(lowScoreCount, reviewCount),
                recent30DayReviewCount,
                recent30DayLowScoreCount);
    }

    @Override
    public ReviewAppealEligibilityRespDTO getAppealEligibility(Long reviewId) {
        Long providerId = requireCurrentUser();
        requireCaretakerRole();
        ReviewDO review = requireReview(reviewId);
        ensureReviewBelongsToProvider(review, providerId);
        return buildEligibility(review);
    }

    @Override
    public ReviewAppealRespDTO getLatestAppeal(Long reviewId) {
        Long providerId = requireCurrentUser();
        requireCaretakerRole();
        ReviewDO review = requireReview(reviewId);
        ensureReviewBelongsToProvider(review, providerId);
        ReviewAppealDO appeal = reviewAppealDao.selectLatestByReviewId(reviewId);
        if (appeal == null || !providerId.equals(appeal.getProviderId())) {
            return null;
        }
        return buildReviewAppealResp(appeal);
    }

    @Override
    @Transactional
    public SubmitReviewAppealRespDTO submitAppeal(Long reviewId, SubmitReviewAppealReqDTO reqDTO) {
        if (reqDTO == null || isBlank(reqDTO.getReason())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long providerId = requireCurrentUser();
        requireCaretakerRole();
        ReviewDO review = requireReview(reviewId);
        ensureReviewBelongsToProvider(review, providerId);
        ReviewAppealEligibilityRespDTO eligibility = buildEligibility(review);
        if (!Boolean.TRUE.equals(eligibility.canAppeal())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        ReviewAppealDO appeal = new ReviewAppealDO();
        appeal.setReviewId(review.getReviewId());
        appeal.setOrderId(review.getOrderId());
        appeal.setProviderId(providerId);
        appeal.setOwnerId(review.getReviewerId());
        appeal.setReason(reqDTO.getReason().trim());
        appeal.setEvidenceUrls(joinEvidenceUrls(reqDTO.getEvidenceUrls()));
        appeal.setAppealStatus(APPEAL_STATUS_PENDING);
        appeal.setAppealDeadline(eligibility.appealDeadline());
        reviewAppealDao.insert(appeal);

        ReviewDO update = new ReviewDO();
        update.setReviewId(review.getReviewId());
        update.setReviewStatus(REVIEW_STATUS_APPEALING);
        reviewDao.updateById(update);
        return new SubmitReviewAppealRespDTO(
                appeal.getAppealId(),
                appeal.getAppealStatus(),
                appealStatusDesc(appeal.getAppealStatus()));
    }

    @Override
    public AdminReviewAppealPageRespDTO listAdminAppeals(Integer page, Integer pageSize, Integer appealStatus) {
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        IPage<ReviewAppealDO> appealPage = reviewAppealDao.selectPage(currentPage, currentPageSize, appealStatus);
        List<ReviewAppealRespDTO> items = appealPage.getRecords().stream()
                .map(this::buildReviewAppealResp)
                .toList();
        return new AdminReviewAppealPageRespDTO(appealPage.getTotal(), currentPage, currentPageSize, items);
    }

    @Override
    public ReviewAppealRespDTO getAdminAppealDetail(Long appealId) {
        ReviewAppealDO appeal = reviewAppealDao.selectById(appealId);
        if (appeal == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return buildReviewAppealResp(appeal);
    }

    @Override
    public void updateReviewStatus(Long reviewId, UpdateReviewStatusReqDTO reqDTO) {
        if (reqDTO == null || !isValidReviewStatus(reqDTO.getReviewStatus())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        requireReview(reviewId);
        ReviewDO update = new ReviewDO();
        update.setReviewId(reviewId);
        update.setReviewStatus(reqDTO.getReviewStatus());
        reviewDao.updateById(update);
    }

    @Override
    @Transactional
    public void updateAppealStatus(Long appealId, UpdateReviewAppealStatusReqDTO reqDTO) {
        if (reqDTO == null || !isValidAppealStatus(reqDTO.getAppealStatus())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        ReviewAppealDO appeal = reviewAppealDao.selectById(appealId);
        if (appeal == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Integer previousStatus = appeal.getAppealStatus();
        ReviewAppealDO update = new ReviewAppealDO();
        update.setAppealId(appealId);
        update.setAppealStatus(reqDTO.getAppealStatus());
        update.setAdminMemo(normalizeText(reqDTO.getAdminMemo()));
        if (isTerminalAppealStatus(reqDTO.getAppealStatus())) {
            update.setClosedAt(LocalDateTime.now());
        }
        reviewAppealDao.updateById(update);

        if (APPEAL_STATUS_SUCCESS == reqDTO.getAppealStatus()) {
            updateReviewStatusInternal(appeal.getReviewId(), REVIEW_STATUS_APPEAL_SUCCESS);
            if (previousStatus == null || APPEAL_STATUS_SUCCESS != previousStatus) {
                applyAppealCreditCorrection(appeal);
            }
            return;
        }
        if (APPEAL_STATUS_FAILED == reqDTO.getAppealStatus()) {
            updateReviewStatusInternal(appeal.getReviewId(), REVIEW_STATUS_APPEAL_REJECTED);
        }
    }

    private ReviewContext loadReviewContext(List<ReviewDO> reviews) {
        List<Long> reviewIds = reviews.stream().map(ReviewDO::getReviewId).toList();
        List<Long> orderIds = reviews.stream().map(ReviewDO::getOrderId).filter(Objects::nonNull).distinct().toList();
        Map<Long, OrderDO> orders = orderIds.stream()
                .map(orderDao::selectById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(OrderDO::getOrderId, Function.identity(), (left, right) -> left));
        Map<Long, List<OrderPetSnapshotDO>> pets = orderPetSnapshotDao.selectByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(OrderPetSnapshotDO::getOrderId));
        Map<Long, List<ReviewDeductionReasonDO>> reasons = reviewDeductionReasonDao.selectByReviewIds(reviewIds).stream()
                .collect(Collectors.groupingBy(ReviewDeductionReasonDO::getReviewId));
        Map<Long, List<ReviewAttachmentDO>> attachments = reviewAttachmentDao.selectByReviewIds(reviewIds).stream()
                .collect(Collectors.groupingBy(ReviewAttachmentDO::getReviewId));
        return new ReviewContext(orders, pets, reasons, attachments);
    }

    private CaretakerReviewItemRespDTO buildReviewItem(ReviewDO review, ReviewContext context) {
        OrderDO order = context.orders().get(review.getOrderId());
        ReviewAppealEligibilityRespDTO eligibility = buildEligibility(review);
        return new CaretakerReviewItemRespDTO(
                review.getReviewId(),
                review.getOrderId(),
                order == null ? null : order.getServiceDate(),
                toPetBriefs(context.pets().get(review.getOrderId())),
                resolveOverallScore(review),
                review.getPunctualityScore(),
                review.getProfessionalScore(),
                review.getContent(),
                toReasonResp(context.reasons().get(review.getReviewId())),
                resolveCreditDeductionScore(review),
                toAttachmentResp(context.attachments().get(review.getReviewId())),
                review.getReviewStatus(),
                reviewStatusDesc(review.getReviewStatus()),
                eligibility.canAppeal(),
                eligibility.unavailableReason(),
                eligibility.appealDeadline(),
                review.getCreatedAt());
    }

    private ReviewAppealRespDTO buildReviewAppealResp(ReviewAppealDO appeal) {
        return new ReviewAppealRespDTO(
                appeal.getAppealId(),
                appeal.getReviewId(),
                appeal.getOrderId(),
                appeal.getProviderId(),
                appeal.getOwnerId(),
                appeal.getReason(),
                splitEvidenceUrls(appeal.getEvidenceUrls()),
                appeal.getAppealStatus(),
                appealStatusDesc(appeal.getAppealStatus()),
                appeal.getAdminMemo(),
                appeal.getAppealDeadline(),
                appeal.getClosedAt(),
                appeal.getCreatedAt());
    }

    private Integer resolveCreditDeductionScore(ReviewDO review) {
        if (review == null || review.getTargetId() == null || review.getOrderId() == null) {
            return 0;
        }
        List<CreditRecordDO> records = creditRecordDao.selectByProviderAndRelation(
                review.getTargetId(), review.getOrderId());
        if (records.isEmpty()) {
            return 0;
        }
        int penaltyScore = records.stream()
                .filter(record -> record.getChangeScore() != null && record.getChangeScore() < 0)
                .filter(record -> isReviewPenaltyReasonType(record.getReasonType()))
                .mapToInt(record -> Math.abs(record.getChangeScore()))
                .sum();
        int correctionScore = records.stream()
                .filter(record -> record.getChangeScore() != null && record.getChangeScore() > 0)
                .filter(record -> Objects.equals(record.getReasonType(),
                        CreditActionTypeEnum.APPEAL_CORRECTION.getCode()))
                .mapToInt(CreditRecordDO::getChangeScore)
                .sum();
        return Math.max(penaltyScore - correctionScore, 0);
    }

    private List<ReviewPetBriefRespDTO> toPetBriefs(List<OrderPetSnapshotDO> pets) {
        if (pets == null || pets.isEmpty()) {
            return List.of();
        }
        return pets.stream()
                .map(pet -> new ReviewPetBriefRespDTO(
                        pet.getSnapPetName(),
                        pet.getSnapPetType(),
                        PetTypeEnum.getDescByCode(pet.getSnapPetType())))
                .toList();
    }

    private List<ReviewDeductionReasonRespDTO> toReasonResp(List<ReviewDeductionReasonDO> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return List.of();
        }
        return reasons.stream()
                .map(reason -> new ReviewDeductionReasonRespDTO(
                        reason.getReasonType(), reasonTypeDesc(reason.getReasonType()), resolveReasonText(reason)))
                .toList();
    }

    private String resolveReasonText(ReviewDeductionReasonDO reason) {
        if (!isBlank(reason.getReasonText())) {
            return reason.getReasonText();
        }
        ReviewDeductionReasonEnum reasonEnum = ReviewDeductionReasonEnum.fromCode(reason.getReasonType());
        return reasonEnum == null ? null : reasonEnum.getDesc();
    }

    private List<ReviewAttachmentRespDTO> toAttachmentResp(List<ReviewAttachmentDO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .map(attachment -> new ReviewAttachmentRespDTO(
                        attachment.getAttachmentId(),
                        attachment.getUrl(),
                        attachment.getObjectKey(),
                        attachment.getMediaType(),
                        attachment.getContentType(),
                        attachment.getFileSize(),
                        attachment.getSortOrder()))
                .toList();
    }

    private ReviewAppealEligibilityRespDTO buildEligibility(ReviewDO review) {
        LocalDateTime appealDeadline = review.getCreatedAt() == null
                ? null
                : review.getCreatedAt().plusDays(APPEAL_WINDOW_DAYS);
        if (review.getTargetId() == null || !review.getTargetId().equals(UserContext.getUserId())) {
            return new ReviewAppealEligibilityRespDTO(review.getReviewId(), false, "评价不属于当前服务者", appealDeadline);
        }
        if (LOW_SCORE_YES != safeInt(review.getIsLowScore())) {
            return new ReviewAppealEligibilityRespDTO(review.getReviewId(), false, "非低分评价不可申诉", appealDeadline);
        }
        if (!reviewDeductionReasonDao.existsByReviewId(review.getReviewId())) {
            return new ReviewAppealEligibilityRespDTO(review.getReviewId(), false, "评价没有扣分理由", appealDeadline);
        }
        if (appealDeadline != null && LocalDateTime.now().isAfter(appealDeadline)) {
            return new ReviewAppealEligibilityRespDTO(review.getReviewId(), false, "已超过申诉期限", appealDeadline);
        }
        if (reviewAppealDao.existsActiveByReviewId(review.getReviewId())
                || REVIEW_STATUS_APPEALING == safeInt(review.getReviewStatus())) {
            return new ReviewAppealEligibilityRespDTO(review.getReviewId(), false, "已有进行中的申诉", appealDeadline);
        }
        return new ReviewAppealEligibilityRespDTO(review.getReviewId(), true, null, appealDeadline);
    }

    private ReviewDO requireReview(Long reviewId) {
        ReviewDO review = reviewDao.selectById(reviewId);
        if (review == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return review;
    }

    private void ensureReviewBelongsToProvider(ReviewDO review, Long providerId) {
        if (review.getTargetId() == null || !review.getTargetId().equals(providerId)
                || REVIEW_TYPE_OWNER_TO_PROVIDER != safeInt(review.getReviewType())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private Long requireCurrentUser() {
        Long currentUser = UserContext.getUserId();
        if (currentUser == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return currentUser;
    }

    private void requireCaretakerRole() {
        Integer roleType = UserContext.getRoleType();
        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR);
        }
    }

    private Integer resolveOverallScore(ReviewDO review) {
        return review.getOverallScore() == null ? review.getScore() : review.getOverallScore();
    }

    private BigDecimal avg(List<ReviewDO> reviews, Function<ReviewDO, Integer> getter) {
        List<Integer> scores = reviews.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .toList();
        if (scores.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal sum = scores.stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(4);
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private boolean isValidReviewStatus(Integer reviewStatus) {
        return reviewStatus != null && reviewStatus >= REVIEW_STATUS_NORMAL && reviewStatus <= 6;
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

    private String appealStatusDesc(Integer appealStatus) {
        return switch (safeInt(appealStatus)) {
            case APPEAL_STATUS_PENDING -> "待仲裁";
            case APPEAL_STATUS_EVIDENCE_COLLECTING -> "取证中";
            case APPEAL_STATUS_DECIDED -> "已判定";
            case APPEAL_STATUS_SUCCESS -> "申诉成立";
            case APPEAL_STATUS_FAILED -> "申诉失败";
            default -> null;
        };
    }

    private String reasonTypeDesc(Integer reasonType) {
        ReviewDeductionReasonEnum reasonEnum = ReviewDeductionReasonEnum.fromCode(reasonType);
        return reasonEnum == null ? null : reasonEnum.getDesc();
    }

    private boolean isReviewPenaltyReasonType(Integer reasonType) {
        return reasonType != null
                && reasonType >= CreditActionTypeEnum.NOT_ON_TIME.getCode()
                && reasonType <= CreditActionTypeEnum.SERIOUS_ACCIDENT.getCode();
    }

    private boolean isValidAppealStatus(Integer appealStatus) {
        return appealStatus != null
                && appealStatus >= APPEAL_STATUS_PENDING
                && appealStatus <= APPEAL_STATUS_FAILED;
    }

    private boolean isTerminalAppealStatus(Integer appealStatus) {
        return APPEAL_STATUS_SUCCESS == safeInt(appealStatus)
                || APPEAL_STATUS_FAILED == safeInt(appealStatus);
    }

    private void updateReviewStatusInternal(Long reviewId, Integer reviewStatus) {
        ReviewDO update = new ReviewDO();
        update.setReviewId(reviewId);
        update.setReviewStatus(reviewStatus);
        reviewDao.updateById(update);
    }

    private void applyAppealCreditCorrection(ReviewAppealDO appeal) {
        if (appeal.getProviderId() == null || appeal.getOrderId() == null) {
            return;
        }
        int correctionScore = creditRecordDao.selectByProviderAndRelation(appeal.getProviderId(), appeal.getOrderId())
                .stream()
                .filter(record -> record.getChangeScore() != null && record.getChangeScore() < 0)
                .filter(record -> record.getReasonType() != null
                        && record.getReasonType() >= CreditActionTypeEnum.NOT_ON_TIME.getCode()
                        && record.getReasonType() <= CreditActionTypeEnum.SERIOUS_ACCIDENT.getCode())
                .mapToInt(record -> Math.abs(record.getChangeScore()))
                .sum();
        if (correctionScore <= 0) {
            return;
        }

        SitterProfileDO profile = sitterProfileDao.selectById(appeal.getProviderId());
        if (profile == null) {
            return;
        }
        int currentScore = profile.getCreditScore() == null ? 80 : profile.getCreditScore();
        int scoreAfter = Math.min(100, currentScore + correctionScore);
        int actualChange = scoreAfter - currentScore;
        if (actualChange <= 0) {
            return;
        }
        profile.setCreditScore(scoreAfter);
        sitterProfileDao.updateById(profile);

        CreditRecordDO correction = new CreditRecordDO();
        correction.setProviderId(appeal.getProviderId());
        correction.setChangeScore(actualChange);
        correction.setScoreAfter(scoreAfter);
        correction.setReasonType(CreditActionTypeEnum.APPEAL_CORRECTION.getCode());
        correction.setRelationId(appeal.getOrderId());
        creditRecordDao.insert(correction);
    }

    private String joinEvidenceUrls(List<String> evidenceUrls) {
        if (evidenceUrls == null || evidenceUrls.isEmpty()) {
            return null;
        }
        return evidenceUrls.stream()
                .filter(url -> !isBlank(url))
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private List<String> splitEvidenceUrls(String evidenceUrls) {
        if (isBlank(evidenceUrls)) {
            return List.of();
        }
        return Arrays.stream(evidenceUrls.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .toList();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private record ReviewContext(
            Map<Long, OrderDO> orders,
            Map<Long, List<OrderPetSnapshotDO>> pets,
            Map<Long, List<ReviewDeductionReasonDO>> reasons,
            Map<Long, List<ReviewAttachmentDO>> attachments) {
    }
}
