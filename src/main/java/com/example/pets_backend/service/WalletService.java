package com.example.pets_backend.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pets_backend.config.AlipayProperties;
import com.example.pets_backend.dao.FinancialLogDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.WithdrawalRecordDao;
import com.example.pets_backend.dao.entity.FinancialLogDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dao.entity.WithdrawalRecordDO;
import com.example.pets_backend.dto.req.CaretakerWalletWithdrawReqDTO;
import com.example.pets_backend.dto.req.WalletRechargeReqDTO;
import com.example.pets_backend.dto.req.WalletWithdrawReqDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletBalanceRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletRecordRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletRecordsRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletWithdrawRespDTO;
import com.example.pets_backend.dto.resp.WalletRechargeConfirmRespDTO;
import com.example.pets_backend.dto.resp.WalletRechargeRespDTO;
import com.example.pets_backend.dto.resp.WalletWithdrawRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final String PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";
    private static final DateTimeFormatter OUT_BIZ_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter RECORD_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PAGE_DEFAULT = 1;
    private static final int PAGE_SIZE_DEFAULT = 20;
    private static final int TRADE_TYPE_ORDER_INCOME = 11;
    private static final int TRADE_TYPE_EMPTY_RUN_COMPENSATION = 12;
    private static final int TRADE_TYPE_WITHDRAW = 13;
    private static final int TRADE_TYPE_ORDER_PAYMENT = 21;
    private static final int TRADE_TYPE_ORDER_REFUND = 22;
    private static final int TRADE_TYPE_RECHARGE = 31;
    private static final int TRADE_TYPE_DEPOSIT_RECHARGE = 41;
    private static final int TRADE_TYPE_DEPOSIT_REFUND_APPLY = 42;
    private static final int TRADE_TYPE_DEPOSIT_REFUND = 43;
    private static final int WITHDRAW_STATUS_PENDING = 0;
    private static final int ACCOUNT_TYPE_ALIPAY = 1;
    private static final long PAY_FORM_TTL_MILLIS = 30L * 60L * 1000L;

    private final Set<String> processedOutTrades = ConcurrentHashMap.newKeySet();
    private final Map<String, PendingPayForm> pendingPayForms = new ConcurrentHashMap<>();

    private final UserDao userDao;
    private final FinancialLogDao financialLogDao;
    private final WithdrawalRecordDao withdrawalRecordDao;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final ObjectMapper objectMapper;

    public WalletRechargeRespDTO createRecharge(WalletRechargeReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        BigDecimal amount = normalizeAmount(reqDTO.amount());
        String outTradeNo = buildOutTradeNo("RC", userId);
        String subject = reqDTO.subject() == null || reqDTO.subject().isBlank() ? "账户充值" : reqDTO.subject();
        String passbackParams = encodePassbackParams(Map.of(
                "userId", String.valueOf(userId),
                "purpose", "RECHARGE"));

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setBizContent(toJson(new LinkedHashMap<>() {{
            put("out_trade_no", outTradeNo);
            put("total_amount", amount.toPlainString());
            put("subject", subject);
            put("product_code", PRODUCT_CODE);
            put("passback_params", passbackParams);
        }}));

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (!response.isSuccess()) {
                throw new ClientException(BaseErrorCode.SERVICE_ERROR);
            }
            pendingPayForms.put(outTradeNo, new PendingPayForm(
                    response.getBody(),
                    userId,
                    System.currentTimeMillis() + PAY_FORM_TTL_MILLIS));
            return new WalletRechargeRespDTO(outTradeNo, response.getBody());
        } catch (AlipayApiException e) {
            throw new ClientException("支付宝充值请求失败", e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    public String getPayFormHtml(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isBlank() || !outTradeNo.startsWith("RC")) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        PendingPayForm payForm = pendingPayForms.get(outTradeNo);
        if (payForm == null || payForm.isExpired()) {
            pendingPayForms.remove(outTradeNo);
            throw new ClientException("支付页面已过期，请返回 App 重新发起充值", BaseErrorCode.CLIENT_ERROR);
        }
        return payForm.html();
    }

    public WalletRechargeConfirmRespDTO confirmRecharge(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        if (!outTradeNo.startsWith("RC" + userId)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        UserDO user = requireUser(userId);
        BigDecimal balanceBefore = safeAmount(user.getBalance());
        Map<String, String> notifyParams = queryRechargeNotifyParams(outTradeNo, userId);
        if (notifyParams == null) {
            return new WalletRechargeConfirmRespDTO(formatAmount(balanceBefore), false);
        }
        handleRechargeNotify(notifyParams);
        user = requireUser(userId);
        BigDecimal balanceAfter = safeAmount(user.getBalance());
        boolean paid = balanceAfter.compareTo(balanceBefore) > 0
                || processedOutTrades.contains(outTradeNo);
        return new WalletRechargeConfirmRespDTO(formatAmount(balanceAfter), paid);
    }

    @Transactional
    public void handleRechargeNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        String tradeStatus = params.get("trade_status");
        if (!Objects.equals(tradeStatus, "TRADE_SUCCESS") && !Objects.equals(tradeStatus, "TRADE_FINISHED")) {
            return;
        }
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo == null || outTradeNo.isBlank()) {
            return;
        }
        if (!processedOutTrades.add(outTradeNo)) {
            return;
        }
        String totalAmount = params.get("total_amount");
        String passbackParams = decodePassbackParams(params.get("passback_params"));
        Map<String, String> passback = splitPassbackParams(passbackParams);
        if (!"RECHARGE".equals(passback.get("purpose"))) {
            return;
        }
        Long userId = parseUserId(passback.get("userId"));
        if (userId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        BigDecimal amount = normalizeAmount(new BigDecimal(totalAmount));
        creditBalance(userId, amount);
    }

    public CaretakerWalletBalanceRespDTO getCaretakerBalance() {
        return getCurrentUserBalance();
    }

    public CaretakerWalletBalanceRespDTO getCurrentUserBalance() {
        UserDO user = requireUser(currentUserId());
        return new CaretakerWalletBalanceRespDTO(formatAmount(safeAmount(user.getBalance())));
    }

    @Transactional
    public CaretakerWalletWithdrawRespDTO applyCaretakerWithdraw(CaretakerWalletWithdrawReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        BigDecimal amount = normalizeAmount(reqDTO.amount());
        UserDO user = requireUser(userId);
        BigDecimal balance = safeAmount(user.getBalance());
        if (balance.compareTo(amount) < 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        WithdrawalRecordDO withdrawalRecord = new WithdrawalRecordDO();
        withdrawalRecord.setUserId(userId);
        withdrawalRecord.setAmount(amount);
        withdrawalRecord.setWithdrawalStatus(WITHDRAW_STATUS_PENDING);
        withdrawalRecord.setAccountType(ACCOUNT_TYPE_ALIPAY);
        withdrawalRecord.setAccountInfo(buildDefaultAccountInfo(user));
        withdrawalRecordDao.insert(withdrawalRecord);

        BigDecimal balanceAfter = balance.subtract(amount);
        user.setBalance(balanceAfter);
        user.setFrozenAmount(safeAmount(user.getFrozenAmount()).add(amount));
        userDao.updateById(user);
        insertFinancialLog(userId, amount.negate(), balanceAfter, TRADE_TYPE_WITHDRAW,
                withdrawalRecord.getWithdrawId());

        return new CaretakerWalletWithdrawRespDTO(formatAmount(balanceAfter));
    }

    public CaretakerWalletRecordsRespDTO listCaretakerWalletRecords(Integer page, Integer pageSize) {
        int pageNum = normalizePage(page);
        int size = normalizePageSize(pageSize);
        IPage<FinancialLogDO> recordPage = financialLogDao.selectByUserId(currentUserId(), pageNum, size);
        return new CaretakerWalletRecordsRespDTO(
                recordPage.getTotal(),
                pageNum,
                size,
                recordPage.getRecords().stream()
                        .map(this::toWalletRecord)
                        .toList());
    }

    public WalletWithdrawRespDTO withdraw(WalletWithdrawReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        BigDecimal amount = normalizeAmount(reqDTO.amount());
        if (reqDTO.payeeAccount() == null || reqDTO.payeeAccount().isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        UserDO user = requireUser(userId);
        BigDecimal balance = safeAmount(user.getBalance());
        if (balance.compareTo(amount) < 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }

        String outBizNo = buildOutTradeNo("WD", userId);
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
        model.setOutBizNo(outBizNo);
        model.setPayeeType("ALIPAY_LOGONID");
        model.setPayeeAccount(reqDTO.payeeAccount());
        model.setAmount(amount.toPlainString());
        model.setPayerShowName("宠托平台");
        model.setRemark(reqDTO.remark() == null ? "用户提现" : reqDTO.remark());
        if (reqDTO.payeeRealName() != null && !reqDTO.payeeRealName().isBlank()) {
            model.setPayeeRealName(reqDTO.payeeRealName());
        }
        request.setBizModel(model);

        try {
            AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                throw new ClientException(BaseErrorCode.SERVICE_ERROR);
            }
            debitBalance(userId, amount);
            return new WalletWithdrawRespDTO(outBizNo, response.getCode(), response.getOrderId());
        } catch (AlipayApiException e) {
            throw new ClientException("支付宝提现请求失败", e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    private Map<String, String> queryRechargeNotifyParams(String outTradeNo, Long userId) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(toJson(Map.of("out_trade_no", outTradeNo)));
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                return null;
            }
            String tradeStatus = response.getTradeStatus();
            if (!Objects.equals(tradeStatus, "TRADE_SUCCESS")
                    && !Objects.equals(tradeStatus, "TRADE_FINISHED")) {
                return null;
            }
            return Map.of(
                    "trade_status", tradeStatus,
                    "out_trade_no", outTradeNo,
                    "total_amount", response.getTotalAmount(),
                    "passback_params", encodePassbackParams(Map.of(
                            "userId", String.valueOf(userId),
                            "purpose", "RECHARGE")));
        } catch (AlipayApiException e) {
            throw new ClientException("查询支付宝充值订单失败", e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    private void creditBalance(Long userId, BigDecimal amount) {
        UserDO user = requireUser(userId);
        BigDecimal balanceAfter = safeAmount(user.getBalance()).add(amount);
        user.setBalance(balanceAfter);
        userDao.updateById(user);
        insertFinancialLog(userId, amount, balanceAfter, TRADE_TYPE_RECHARGE, null);
    }

    private void debitBalance(Long userId, BigDecimal amount) {
        UserDO user = requireUser(userId);
        BigDecimal balance = safeAmount(user.getBalance());
        if (balance.compareTo(amount) < 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        BigDecimal balanceAfter = balance.subtract(amount);
        user.setBalance(balanceAfter);
        userDao.updateById(user);
        insertFinancialLog(userId, amount.negate(), balanceAfter, TRADE_TYPE_WITHDRAW, null);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return amount.setScale(2, RoundingMode.DOWN);
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

    private CaretakerWalletRecordRespDTO toWalletRecord(FinancialLogDO financialLog) {
        BigDecimal amount = safeAmount(financialLog.getAmount());
        return new CaretakerWalletRecordRespDTO(
                String.valueOf(financialLog.getLogId()),
                financialLog.getTradeType(),
                tradeTypeText(financialLog.getTradeType()),
                amount.compareTo(BigDecimal.ZERO) >= 0 ? 1 : 2,
                formatAmount(amount.abs()),
                buildRecordDescription(financialLog),
                financialLog.getCreatedAt() == null ? null : RECORD_TIME_FORMATTER.format(financialLog.getCreatedAt()));
    }

    private String tradeTypeText(Integer tradeType) {
        if (tradeType == null) {
            return "Unknown";
        }
        return switch (tradeType) {
            case TRADE_TYPE_ORDER_INCOME -> "Order income";
            case TRADE_TYPE_EMPTY_RUN_COMPENSATION -> "Empty run compensation";
            case TRADE_TYPE_WITHDRAW -> "Withdraw";
            case TRADE_TYPE_ORDER_PAYMENT -> "Order payment";
            case TRADE_TYPE_ORDER_REFUND -> "Order refund";
            case TRADE_TYPE_RECHARGE -> "Recharge";
            case TRADE_TYPE_DEPOSIT_RECHARGE -> "Deposit recharge";
            case TRADE_TYPE_DEPOSIT_REFUND_APPLY -> "Deposit refund applying";
            case TRADE_TYPE_DEPOSIT_REFUND -> "Deposit refund";
            default -> "Wallet transaction";
        };
    }

    private String buildRecordDescription(FinancialLogDO financialLog) {
        String typeText = tradeTypeText(financialLog.getTradeType());
        if (financialLog.getRelationId() == null) {
            return typeText;
        }
        return typeText + " #" + financialLog.getRelationId();
    }

    private String formatAmount(BigDecimal amount) {
        return safeAmount(amount).setScale(2, RoundingMode.DOWN).toPlainString();
    }

    private String buildDefaultAccountInfo(UserDO user) {
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            return "alipay:" + user.getPhone();
        }
        return "alipay:pending";
    }

    private int normalizePage(Integer page) {
        return page != null && page > 0 ? page : PAGE_DEFAULT;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize != null && pageSize > 0 ? pageSize : PAGE_SIZE_DEFAULT;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private String buildOutTradeNo(String prefix, Long userId) {
        String timestamp = LocalDateTime.now().format(OUT_BIZ_NO_FORMATTER);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return prefix + userId + timestamp + random;
    }

    private String encodePassbackParams(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return URLEncoder.encode(builder.toString(), StandardCharsets.UTF_8);
    }

    private String decodePassbackParams(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private Map<String, String> splitPassbackParams(String value) {
        Map<String, String> result = new LinkedHashMap<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        String[] pairs = value.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }

    private Long parseUserId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private record PendingPayForm(String html, Long userId, long expiresAtMillis) {
        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }
}
