package com.example.pets_backend.service;

import com.example.pets_backend.dao.ArbitrationRecordDao;
import com.example.pets_backend.dao.ExceptionReportDao;
import com.example.pets_backend.dao.FinancialLogDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.FinancialLogDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.CaretakerDepositRechargeReqDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRechargeRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRefundApplyRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRefundRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRefundStatusRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaretakerDepositService {

    public static final int TARGET_LEVEL_BASIC = 1;
    public static final int TARGET_LEVEL_PREMIUM_DOG = 2;

    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int VERIFY_STATUS_INIT = 0;
    private static final int DEFAULT_CREDIT_SCORE = 80;
    private static final int DEFAULT_SERVICE_RANGE_KM = 5;
    private static final int DEFAULT_BANNED_STATUS = 0;
    private static final int PET_TYPE_DOG = 2;
    private static final int TRADE_TYPE_DEPOSIT_RECHARGE = 41;
    private static final int TRADE_TYPE_DEPOSIT_REFUND_APPLY = 42;
    private static final int TRADE_TYPE_DEPOSIT_REFUND = 43;
    private static final int REFUND_COOLING_DAYS = 15;
    private static final int LOW_CREDIT_SCORE_THRESHOLD = 60;
    private static final int REFUND_RATE_LOW_CREDIT_PERCENT = 70;
    private static final int REFUND_RATE_FULL_PERCENT = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final BigDecimal BASE_DEPOSIT_AMOUNT = new BigDecimal("200.00");
    private static final BigDecimal PREMIUM_DOG_DEPOSIT_AMOUNT = new BigDecimal("500.00");
    private static final BigDecimal REFUND_RATE_LOW_CREDIT = new BigDecimal("0.70");
    private static final BigDecimal REFUND_RATE_FULL = BigDecimal.ONE;

    private static final String CUSTODY_RULE = "保证金由第三方支付平台独立账户托管，仅在仲裁裁定后根据结果强制扣划";

    private static final List<String> RARE_DOG_KEYWORDS = List.of(
            "名贵犬",
            "名贵",
            "稀有犬",
            "赛级犬",
            "纯种犬",
            "血统犬");

    private static final String BLOCK_REASON_ACTIVE_DISPUTE = "存在进行中的纠纷订单";
    private static final String BLOCK_REASON_PENDING_COMPLAINT = "存在未处理的投诉单";
    private static final String BLOCK_REASON_UNCLOSED_CLAIM = "存在未结案的理赔单";

    private final UserDao userDao;
    private final SitterProfileDao sitterProfileDao;
    private final FinancialLogDao financialLogDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final OrderDao orderDao;
    private final ArbitrationRecordDao arbitrationRecordDao;
    private final ExceptionReportDao exceptionReportDao;

    public CaretakerDepositRespDTO getMyDeposit() {
        Long providerId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(providerId);
        SitterProfileDO profile = ensureSitterProfile(providerId);
        BigDecimal depositAmount = safeAmount(profile.getDepositAmount());
        return buildDepositResp(depositAmount, user);
    }

    @Transactional
    public CaretakerDepositRechargeRespDTO recharge(CaretakerDepositRechargeReqDTO reqDTO) {
        Long providerId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(providerId);
        SitterProfileDO profile = ensureSitterProfile(providerId);

        Integer targetLevel = reqDTO == null ? TARGET_LEVEL_BASIC : reqDTO.targetLevel();
        BigDecimal targetDepositAmount = resolveTargetDeposit(targetLevel);
        BigDecimal currentDepositAmount = safeAmount(profile.getDepositAmount());
        if (currentDepositAmount.compareTo(targetDepositAmount) >= 0) {
            return new CaretakerDepositRechargeRespDTO(
                    targetLevel,
                    formatAmount(targetDepositAmount),
                    formatAmount(BigDecimal.ZERO),
                    formatAmount(currentDepositAmount),
                    formatAmount(user.getBalance()),
                    formatAmount(user.getFrozenAmount()),
                    true,
                    CUSTODY_RULE);
        }

        BigDecimal deductedAmount = targetDepositAmount.subtract(currentDepositAmount);
        BigDecimal walletBalance = safeAmount(user.getBalance());
        if (walletBalance.compareTo(deductedAmount) < 0) {
            throw new ClientException(BaseErrorCode.CARETAKER_DEPOSIT_BALANCE_NOT_ENOUGH_ERROR);
        }

        BigDecimal walletBalanceAfter = walletBalance.subtract(deductedAmount);
        BigDecimal frozenAmountAfter = safeAmount(user.getFrozenAmount()).add(deductedAmount);

        user.setBalance(walletBalanceAfter);
        user.setFrozenAmount(frozenAmountAfter);
        userDao.updateById(user);

        profile.setDepositAmount(targetDepositAmount);
        sitterProfileDao.updateById(profile);

        insertFinancialLog(providerId, deductedAmount.negate(), walletBalanceAfter, TRADE_TYPE_DEPOSIT_RECHARGE, null);

        return new CaretakerDepositRechargeRespDTO(
                targetLevel,
                formatAmount(targetDepositAmount),
                formatAmount(deductedAmount),
                formatAmount(targetDepositAmount),
                formatAmount(walletBalanceAfter),
                formatAmount(frozenAmountAfter),
                false,
                CUSTODY_RULE);
    }

    @Transactional
    public CaretakerDepositRefundApplyRespDTO applyRefund() {
        Long providerId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(providerId);
        SitterProfileDO profile = ensureSitterProfile(providerId);
        BigDecimal depositAmount = safeAmount(profile.getDepositAmount());
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClientException(BaseErrorCode.CARETAKER_DEPOSIT_EMPTY_ERROR);
        }

        FinancialLogDO pendingApplyLog = findPendingRefundApplyLog(providerId);
        if (pendingApplyLog != null) {
            throw new ClientException(BaseErrorCode.CARETAKER_DEPOSIT_REFUND_PENDING_ERROR);
        }

        List<String> blockReasons = evaluateRefundBlockReasons(providerId);
        if (!blockReasons.isEmpty()) {
            throwRefundBlocked(blockReasons);
        }

        LocalDateTime applyTime = LocalDateTime.now();
        insertFinancialLog(providerId, BigDecimal.ZERO, safeAmount(user.getBalance()),
                TRADE_TYPE_DEPOSIT_REFUND_APPLY, null);

        RefundSettlement settlement = buildRefundSettlement(profile, depositAmount);
        return new CaretakerDepositRefundApplyRespDTO(
                formatDateTime(applyTime),
                formatDateTime(applyTime.plusDays(REFUND_COOLING_DAYS)),
                REFUND_COOLING_DAYS,
                formatAmount(depositAmount),
                formatAmount(settlement.refundAmount()),
                formatAmount(settlement.penaltyAmount()),
                CUSTODY_RULE);
    }

    public CaretakerDepositRefundStatusRespDTO getRefundStatus() {
        Long providerId = currentUserId();
        requireCaretakerRole();
        SitterProfileDO profile = ensureSitterProfile(providerId);
        BigDecimal depositAmount = safeAmount(profile.getDepositAmount());
        RefundSettlement settlement = buildRefundSettlement(profile, depositAmount);
        List<String> blockReasons = evaluateRefundBlockReasons(providerId);
        boolean blocked = !blockReasons.isEmpty();

        FinancialLogDO pendingApplyLog = findPendingRefundApplyLog(providerId);
        if (pendingApplyLog == null) {
            return new CaretakerDepositRefundStatusRespDTO(
                    false,
                    null,
                    null,
                    0L,
                    false,
                    settlement.creditScore(),
                    formatAmount(depositAmount),
                    formatAmount(settlement.refundAmount()),
                    formatAmount(settlement.penaltyAmount()),
                    blocked,
                    blockReasons,
                    CUSTODY_RULE);
        }

        LocalDateTime applyTime = pendingApplyLog.getCreatedAt();
        if (applyTime == null) {
            applyTime = LocalDateTime.now();
        }
        LocalDateTime expectedSettleTime = applyTime.plusDays(REFUND_COOLING_DAYS);
        long remainingSeconds = Math.max(0L, Duration.between(LocalDateTime.now(), expectedSettleTime).getSeconds());
        boolean canSettle = remainingSeconds == 0L
                && !blocked
                && depositAmount.compareTo(BigDecimal.ZERO) > 0;

        return new CaretakerDepositRefundStatusRespDTO(
                true,
                formatDateTime(applyTime),
                formatDateTime(expectedSettleTime),
                remainingSeconds,
                canSettle,
                settlement.creditScore(),
                formatAmount(depositAmount),
                formatAmount(settlement.refundAmount()),
                formatAmount(settlement.penaltyAmount()),
                blocked,
                blockReasons,
                CUSTODY_RULE);
    }

    @Transactional
    public CaretakerDepositRefundRespDTO settleRefund() {
        Long providerId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(providerId);
        SitterProfileDO profile = ensureSitterProfile(providerId);
        BigDecimal depositAmount = safeAmount(profile.getDepositAmount());
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClientException(BaseErrorCode.CARETAKER_DEPOSIT_EMPTY_ERROR);
        }

        FinancialLogDO pendingApplyLog = findPendingRefundApplyLog(providerId);
        if (pendingApplyLog == null) {
            throw new ClientException(BaseErrorCode.CARETAKER_DEPOSIT_REFUND_NOT_APPLIED_ERROR);
        }
        LocalDateTime applyTime = pendingApplyLog.getCreatedAt();
        if (applyTime == null || LocalDateTime.now().isBefore(applyTime.plusDays(REFUND_COOLING_DAYS))) {
            throw new ClientException(BaseErrorCode.CARETAKER_DEPOSIT_REFUND_COOLING_ERROR);
        }

        List<String> blockReasons = evaluateRefundBlockReasons(providerId);
        if (!blockReasons.isEmpty()) {
            throwRefundBlocked(blockReasons);
        }

        RefundSettlement settlement = buildRefundSettlement(profile, depositAmount);
        BigDecimal walletBalanceAfter = safeAmount(user.getBalance()).add(settlement.refundAmount());
        BigDecimal frozenAmountAfter = safeAmount(user.getFrozenAmount()).subtract(depositAmount);
        if (frozenAmountAfter.compareTo(BigDecimal.ZERO) < 0) {
            frozenAmountAfter = BigDecimal.ZERO;
        }

        user.setBalance(walletBalanceAfter);
        user.setFrozenAmount(frozenAmountAfter);
        userDao.updateById(user);

        profile.setDepositAmount(BigDecimal.ZERO);
        sitterProfileDao.updateById(profile);

        insertFinancialLog(providerId, settlement.refundAmount(), walletBalanceAfter, TRADE_TYPE_DEPOSIT_REFUND, null);

        return new CaretakerDepositRefundRespDTO(
                formatAmount(settlement.refundAmount()),
                formatAmount(settlement.penaltyAmount()),
                settlement.refundRatePercent(),
                settlement.creditScore(),
                formatAmount(walletBalanceAfter),
                formatAmount(frozenAmountAfter),
                CUSTODY_RULE);
    }

    public void ensureDepositForOrder(BigDecimal currentDepositAmount, Long orderId) {
        BigDecimal requiredDepositAmount = resolveRequiredDeposit(orderId);
        BigDecimal paidDepositAmount = safeAmount(currentDepositAmount);
        if (paidDepositAmount.compareTo(requiredDepositAmount) >= 0) {
            return;
        }
        if (requiredDepositAmount.compareTo(PREMIUM_DOG_DEPOSIT_AMOUNT) >= 0) {
            throw new ClientException(BaseErrorCode.CARETAKER_PREMIUM_DEPOSIT_REQUIRED_ERROR);
        }
        throw new ClientException(BaseErrorCode.CARETAKER_BASE_DEPOSIT_REQUIRED_ERROR);
    }

    private CaretakerDepositRespDTO buildDepositResp(BigDecimal depositAmount, UserDO user) {
        return new CaretakerDepositRespDTO(
                formatAmount(depositAmount),
                formatAmount(BASE_DEPOSIT_AMOUNT),
                formatAmount(PREMIUM_DOG_DEPOSIT_AMOUNT),
                depositAmount.compareTo(BASE_DEPOSIT_AMOUNT) >= 0,
                depositAmount.compareTo(PREMIUM_DOG_DEPOSIT_AMOUNT) >= 0,
                formatAmount(user.getBalance()),
                formatAmount(user.getFrozenAmount()),
                CUSTODY_RULE);
    }

    private BigDecimal resolveTargetDeposit(Integer targetLevel) {
        if (targetLevel == null || Objects.equals(targetLevel, TARGET_LEVEL_BASIC)) {
            return BASE_DEPOSIT_AMOUNT;
        }
        if (Objects.equals(targetLevel, TARGET_LEVEL_PREMIUM_DOG)) {
            return PREMIUM_DOG_DEPOSIT_AMOUNT;
        }
        throw new ClientException(BaseErrorCode.CLIENT_ERROR);
    }

    private BigDecimal resolveRequiredDeposit(Long orderId) {
        if (orderId == null) {
            return BASE_DEPOSIT_AMOUNT;
        }
        List<OrderPetSnapshotDO> snapshots = orderPetSnapshotDao.selectByOrderIds(List.of(orderId));
        boolean hasRareDog = snapshots.stream().anyMatch(this::isRareDogSnapshot);
        return hasRareDog ? PREMIUM_DOG_DEPOSIT_AMOUNT : BASE_DEPOSIT_AMOUNT;
    }

    private boolean isRareDogSnapshot(OrderPetSnapshotDO snapshot) {
        if (snapshot == null || !Objects.equals(snapshot.getSnapPetType(), PET_TYPE_DOG)) {
            return false;
        }
        String petName = safeText(snapshot.getSnapPetName());
        String requirement = safeText(snapshot.getSnapReq());
        String combined = petName + " " + requirement;
        return RARE_DOG_KEYWORDS.stream().anyMatch(combined::contains);
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }

    private FinancialLogDO findPendingRefundApplyLog(Long userId) {
        FinancialLogDO latestApply = financialLogDao.selectLatestByUserIdAndTradeType(userId,
                TRADE_TYPE_DEPOSIT_REFUND_APPLY);
        if (latestApply == null) {
            return null;
        }
        FinancialLogDO latestRefund = financialLogDao.selectLatestByUserIdAndTradeType(userId,
                TRADE_TYPE_DEPOSIT_REFUND);
        if (latestRefund == null) {
            return latestApply;
        }
        return isLogNewer(latestApply, latestRefund) ? latestApply : null;
    }

    private boolean isLogNewer(FinancialLogDO left, FinancialLogDO right) {
        LocalDateTime leftTime = left.getCreatedAt();
        LocalDateTime rightTime = right.getCreatedAt();
        if (leftTime != null && rightTime != null && !leftTime.isEqual(rightTime)) {
            return leftTime.isAfter(rightTime);
        }
        Long leftId = left.getLogId();
        Long rightId = right.getLogId();
        if (leftId == null || rightId == null) {
            return false;
        }
        return leftId > rightId;
    }

    private RefundSettlement buildRefundSettlement(SitterProfileDO profile, BigDecimal depositAmount) {
        int creditScore = resolveCreditScore(profile);
        boolean lowCredit = creditScore < LOW_CREDIT_SCORE_THRESHOLD;
        BigDecimal refundRate = lowCredit ? REFUND_RATE_LOW_CREDIT : REFUND_RATE_FULL;
        int refundRatePercent = lowCredit ? REFUND_RATE_LOW_CREDIT_PERCENT : REFUND_RATE_FULL_PERCENT;
        BigDecimal normalizedDeposit = safeAmount(depositAmount).setScale(2, RoundingMode.DOWN);
        BigDecimal refundAmount = normalizedDeposit.multiply(refundRate).setScale(2, RoundingMode.DOWN);
        BigDecimal penaltyAmount = normalizedDeposit.subtract(refundAmount).setScale(2, RoundingMode.DOWN);
        return new RefundSettlement(normalizedDeposit, refundAmount, penaltyAmount, creditScore, refundRatePercent);
    }

    private int resolveCreditScore(SitterProfileDO profile) {
        if (profile == null || profile.getCreditScore() == null) {
            return DEFAULT_CREDIT_SCORE;
        }
        return profile.getCreditScore();
    }

    private List<String> evaluateRefundBlockReasons(Long providerId) {
        List<String> reasons = new ArrayList<>();
        if (arbitrationRecordDao.existsActiveByParticipant(providerId)) {
            reasons.add(BLOCK_REASON_ACTIVE_DISPUTE);
        }
        if (existsPendingComplaint(providerId)) {
            reasons.add(BLOCK_REASON_PENDING_COMPLAINT);
        }
        if (arbitrationRecordDao.existsUnclosedClaimByParticipant(providerId)) {
            reasons.add(BLOCK_REASON_UNCLOSED_CLAIM);
        }
        return reasons;
    }

    private boolean existsPendingComplaint(Long providerId) {
        List<Long> providerOrderIds = orderDao.selectByProviderId(providerId).stream()
                .map(OrderDO::getOrderId)
                .filter(Objects::nonNull)
                .toList();
        return exceptionReportDao.existsPendingByOrderIds(providerOrderIds);
    }

    private void throwRefundBlocked(List<String> blockReasons) {
        String message = blockReasons == null || blockReasons.isEmpty()
                ? BaseErrorCode.CARETAKER_DEPOSIT_REFUND_BLOCKED_ERROR.message()
                : "保证金退还被阻断：" + String.join("；", blockReasons);
        throw new ClientException(message, BaseErrorCode.CARETAKER_DEPOSIT_REFUND_BLOCKED_ERROR);
    }

    private SitterProfileDO ensureSitterProfile(Long providerId) {
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        if (profile != null) {
            return profile;
        }
        SitterProfileDO newProfile = new SitterProfileDO();
        newProfile.setProviderId(providerId);
        newProfile.setVerifyStatus(VERIFY_STATUS_INIT);
        newProfile.setDepositAmount(BigDecimal.ZERO);
        newProfile.setCreditScore(DEFAULT_CREDIT_SCORE);
        newProfile.setIsBanned(DEFAULT_BANNED_STATUS);
        newProfile.setServiceRadiusKm(DEFAULT_SERVICE_RANGE_KM);
        sitterProfileDao.insert(newProfile);
        return newProfile;
    }

    private UserDO requireUser(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return user;
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String formatAmount(BigDecimal amount) {
        return safeAmount(amount).setScale(2, RoundingMode.DOWN).toPlainString();
    }

    private String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return DATE_TIME_FORMATTER.format(time);
    }

    private void insertFinancialLog(Long userId, BigDecimal amount, BigDecimal balanceAfter, Integer tradeType,
            Long relationId) {
        FinancialLogDO financialLog = new FinancialLogDO();
        financialLog.setUserId(userId);
        financialLog.setAmount(amount);
        financialLog.setBalanceAfter(balanceAfter);
        financialLog.setTradeType(tradeType);
        financialLog.setRelationId(relationId);
        financialLogDao.insert(financialLog);
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

    private record RefundSettlement(
            BigDecimal depositAmount,
            BigDecimal refundAmount,
            BigDecimal penaltyAmount,
            Integer creditScore,
            Integer refundRatePercent) {
    }
}
