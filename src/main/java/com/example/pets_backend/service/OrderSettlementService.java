package com.example.pets_backend.service;

import com.example.pets_backend.dao.FinancialLogDao;
import com.example.pets_backend.dao.FulfillmentRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderSettlementDao;
import com.example.pets_backend.dao.PlatformFinancialLogDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.FinancialLogDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderSettlementDO;
import com.example.pets_backend.dao.entity.PlatformFinancialLogDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.OrderSettlementRespDTO;
import com.example.pets_backend.enums.OrderSettlementStatusEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.FulfillmentCompletionSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSettlementService {

    private static final int ORDER_STATUS_PENDING_PAY = 2;
    private static final int ORDER_STATUS_PENDING_FULFILL = 3;
    private static final int ORDER_STATUS_PENDING_OWNER_CONFIRMATION = 5;
    private static final int ORDER_STATUS_COMPLETED = 6;
    private static final int SETTLEMENT_STATUS_ESCROWED = 1;
    private static final int SETTLEMENT_STATUS_SETTLED = 2;
    private static final int TRADE_TYPE_ORDER_INCOME = 11;
    private static final int TRADE_TYPE_EMPTY_RUN_COMPENSATION = 12;
    private static final int TRADE_TYPE_ORDER_REFUND = 22;
    private static final int TRADE_TYPE_PLATFORM_COMMISSION = 101;
    private static final BigDecimal PLATFORM_COMMISSION_RATE = new BigDecimal("0.30");
    private static final BigDecimal EMPTY_RUN_COMPENSATION_RATE = new BigDecimal("0.50");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderDao orderDao;
    private final FulfillmentRecordDao fulfillmentRecordDao;
    private final OrderSettlementDao orderSettlementDao;
    private final UserDao userDao;
    private final FinancialLogDao financialLogDao;
    private final PlatformFinancialLogDao platformFinancialLogDao;

    @Transactional
    public void markPaidAndCreateEscrow(Long orderId) {
        OrderDO order = requireOrder(orderId);
        if (Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_FULFILL)) {
            createEscrowIfAbsent(order);
            return;
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new ClientException(BaseErrorCode.ORDER_PAYMENT_ERROR);
        }
        orderDao.updateStatus(orderId, ORDER_STATUS_PENDING_FULFILL);
        createEscrowIfAbsent(order);
    }

    @Transactional
    public OrderSettlementRespDTO ownerConfirmCompletion(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_OWNER_CONFIRMATION)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (!FulfillmentCompletionSupport.areAllChecklistNodesUploaded(order, fulfillmentRecordDao)) {
            throw new ClientException("宠托师尚未完成全部打卡节点，暂无法确认结算");
        }
        if (order.getProviderId() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        OrderSettlementDO settlement = requireEscrowSettlement(order);
        if (Objects.equals(settlement.getSettlementStatus(), SETTLEMENT_STATUS_SETTLED)) {
            return toRespDTO(settlement);
        }

        UserDO provider = requireUser(order.getProviderId());
        BigDecimal providerIncome = safeAmount(settlement.getProviderIncome());
        BigDecimal providerBalanceAfter = safeAmount(provider.getBalance()).add(providerIncome);
        provider.setBalance(providerBalanceAfter);
        userDao.updateById(provider);

        insertFinancialLog(provider.getUserId(), providerIncome, providerBalanceAfter, orderId,
                TRADE_TYPE_ORDER_INCOME);
        insertPlatformCommissionLog(settlement.getCommissionAmount(), orderId);

        LocalDateTime now = LocalDateTime.now();
        settlement.setSettlementStatus(SETTLEMENT_STATUS_SETTLED);
        settlement.setSettledAt(now);
        orderSettlementDao.updateById(settlement);
        orderDao.updateStatus(orderId, ORDER_STATUS_COMPLETED);
        return toRespDTO(settlement);
    }

    @Transactional
    public void settleExceptionEnded(Long orderId) {
        OrderDO order = requireOrder(orderId);
        if (order.getProviderId() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        OrderSettlementDO settlement = requireEscrowSettlement(order);
        if (Objects.equals(settlement.getSettlementStatus(), SETTLEMENT_STATUS_SETTLED)) {
            return;
        }

        BigDecimal grossAmount = normalizeAmount(settlement.getGrossAmount());
        BigDecimal providerCompensation = grossAmount.multiply(EMPTY_RUN_COMPENSATION_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal ownerRefund = grossAmount.subtract(providerCompensation).setScale(2, RoundingMode.HALF_UP);

        UserDO provider = requireUser(order.getProviderId());
        BigDecimal providerBalanceAfter = safeAmount(provider.getBalance()).add(providerCompensation);
        provider.setBalance(providerBalanceAfter);
        userDao.updateById(provider);
        insertFinancialLog(provider.getUserId(), providerCompensation, providerBalanceAfter, orderId,
                TRADE_TYPE_EMPTY_RUN_COMPENSATION);

        UserDO owner = requireUser(order.getOwnerId());
        BigDecimal ownerBalanceAfter = safeAmount(owner.getBalance()).add(ownerRefund);
        owner.setBalance(ownerBalanceAfter);
        userDao.updateById(owner);
        insertFinancialLog(owner.getUserId(), ownerRefund, ownerBalanceAfter, orderId, TRADE_TYPE_ORDER_REFUND);

        LocalDateTime now = LocalDateTime.now();
        settlement.setCommissionRate(BigDecimal.ZERO);
        settlement.setCommissionAmount(BigDecimal.ZERO);
        settlement.setProviderIncome(providerCompensation);
        settlement.setSettlementStatus(SETTLEMENT_STATUS_SETTLED);
        settlement.setSettledAt(now);
        orderSettlementDao.updateById(settlement);

        log.info("Exception-ended settlement completed. orderId={}, providerCompensation={}, ownerRefund={}",
                orderId, providerCompensation, ownerRefund);
    }

    public OrderSettlementRespDTO getSettlement(Long orderId) {
        Long currentUserId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!currentUserId.equals(order.getOwnerId()) && !currentUserId.equals(order.getProviderId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        OrderSettlementDO settlement = orderSettlementDao.selectByOrderId(orderId);
        if (settlement == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return toRespDTO(settlement);
    }

    private void createEscrowIfAbsent(OrderDO order) {
        if (orderSettlementDao.selectByOrderId(order.getOrderId()) != null) {
            return;
        }
        if (order.getProviderId() == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        BigDecimal grossAmount = normalizeAmount(order.getTotalAmount());
        BigDecimal commissionAmount = grossAmount.multiply(PLATFORM_COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerIncome = grossAmount.subtract(commissionAmount).setScale(2, RoundingMode.HALF_UP);

        OrderSettlementDO settlement = new OrderSettlementDO();
        settlement.setOrderId(order.getOrderId());
        settlement.setOwnerId(order.getOwnerId());
        settlement.setProviderId(order.getProviderId());
        settlement.setGrossAmount(grossAmount);
        settlement.setCommissionRate(PLATFORM_COMMISSION_RATE);
        settlement.setCommissionAmount(commissionAmount);
        settlement.setProviderIncome(providerIncome);
        settlement.setSettlementStatus(SETTLEMENT_STATUS_ESCROWED);
        orderSettlementDao.insert(settlement);
    }

    private OrderSettlementDO requireEscrowSettlement(OrderDO order) {
        OrderSettlementDO settlement = orderSettlementDao.selectByOrderId(order.getOrderId());
        if (settlement == null) {
            createEscrowIfAbsent(order);
            settlement = orderSettlementDao.selectByOrderId(order.getOrderId());
        }
        if (settlement == null) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
        return settlement;
    }

    private void insertFinancialLog(Long userId, BigDecimal amount, BigDecimal balanceAfter, Long orderId,
            Integer tradeType) {
        FinancialLogDO financialLog = new FinancialLogDO();
        financialLog.setUserId(userId);
        financialLog.setAmount(amount);
        financialLog.setBalanceAfter(balanceAfter);
        financialLog.setTradeType(tradeType);
        financialLog.setRelationId(orderId);
        financialLogDao.insert(financialLog);
    }

    private void insertPlatformCommissionLog(BigDecimal amount, Long orderId) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        PlatformFinancialLogDO latest = platformFinancialLogDao.selectLatest();
        BigDecimal balanceAfter = (latest == null ? BigDecimal.ZERO : safeAmount(latest.getBalanceAfter()))
                .add(normalizedAmount)
                .setScale(2, RoundingMode.HALF_UP);

        PlatformFinancialLogDO platformFinancialLog = new PlatformFinancialLogDO();
        platformFinancialLog.setAmount(normalizedAmount);
        platformFinancialLog.setBalanceAfter(balanceAfter);
        platformFinancialLog.setTradeType(TRADE_TYPE_PLATFORM_COMMISSION);
        platformFinancialLog.setRelationId(orderId);
        platformFinancialLog.setRemark("Order commission 30%");
        platformFinancialLogDao.insert(platformFinancialLog);
    }

    private OrderSettlementRespDTO toRespDTO(OrderSettlementDO settlement) {
        return new OrderSettlementRespDTO(
                settlement.getSettlementId(),
                settlement.getOrderId(),
                settlement.getOwnerId(),
                settlement.getProviderId(),
                formatAmount(settlement.getGrossAmount()),
                formatAmount(settlement.getCommissionRate()),
                formatAmount(settlement.getCommissionAmount()),
                formatAmount(settlement.getProviderIncome()),
                settlement.getSettlementStatus(),
                OrderSettlementStatusEnum.getDescByCode(settlement.getSettlementStatus()),
                settlement.getSettledAt() == null ? null : DATE_TIME_FORMATTER.format(settlement.getSettledAt()));
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    private UserDO requireUser(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return user;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String formatAmount(BigDecimal amount) {
        return safeAmount(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
