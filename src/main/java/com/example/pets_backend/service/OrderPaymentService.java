package com.example.pets_backend.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.example.pets_backend.config.AlipayProperties;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dto.req.OrderAlipayPaymentReqDTO;
import com.example.pets_backend.dto.resp.OrderAlipayAppPaymentRespDTO;
import com.example.pets_backend.dto.resp.OrderAlipayPaymentRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private static final int ORDER_STATUS_PENDING_PAY = 2;
    private static final int ORDER_STATUS_PENDING_FULFILL = 3;
    private static final String PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";
    private static final String PAGE_PAY_CHANNEL = "ALIPAY_PAGE";
    private static final String APP_PAY_CHANNEL = "ALIPAY_APP";
    private static final DateTimeFormatter PAYMENT_ID_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderDao orderDao;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final ObjectMapper objectMapper;
    private final OrderSettlementService orderSettlementService;

    public OrderAlipayPaymentRespDTO createAlipayPayment(Long orderId, OrderAlipayPaymentReqDTO reqDTO) {
        return createAlipayPagePayment(orderId, reqDTO);
    }

    public OrderAlipayPaymentRespDTO createAlipayPagePayment(Long orderId, OrderAlipayPaymentReqDTO reqDTO) {
        String returnUrl = resolvePageReturnUrl(reqDTO);
        String quitUrl = resolvePageQuitUrl(reqDTO);
        if (isBlank(returnUrl) || isBlank(quitUrl)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new ClientException(BaseErrorCode.ORDER_PAYMENT_ERROR);
        }

        String outTradeNo = buildPaymentId(orderId);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(returnUrl);
        request.setBizContent(toJson(new LinkedHashMap<>() {{
            put("out_trade_no", outTradeNo);
            put("total_amount", formatAmount(order.getTotalAmount()));
            put("subject", "Order payment #" + orderId);
            put("product_code", PRODUCT_CODE);
            put("quit_url", quitUrl);
        }}));

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (!response.isSuccess()) {
                throw new ClientException(BaseErrorCode.SERVICE_ERROR);
            }
            return new OrderAlipayPaymentRespDTO(
                    outTradeNo,
                    String.valueOf(orderId),
                    PAGE_PAY_CHANNEL,
                    response.getBody(),
                    RESPONSE_TIME_FORMATTER.format(expiresAt));
        } catch (AlipayApiException e) {
            throw new ClientException("Alipay order payment request failed", e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    public OrderAlipayAppPaymentRespDTO createAlipayAppPayment(Long orderId) {
        Long ownerId = currentUserId();
        OrderDO order = requireOrder(orderId);
        if (!ownerId.equals(order.getOwnerId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            throw new ClientException(BaseErrorCode.ORDER_PAYMENT_ERROR);
        }

        String outTradeNo = buildPaymentId(orderId);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setBizContent(toJson(new LinkedHashMap<>() {{
            put("out_trade_no", outTradeNo);
            put("total_amount", formatAmount(order.getTotalAmount()));
            put("subject", "Order payment #" + orderId);
            put("product_code", PRODUCT_CODE);
        }}));

        try {
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            if (!response.isSuccess()) {
                throw new ClientException(BaseErrorCode.SERVICE_ERROR);
            }
            return new OrderAlipayAppPaymentRespDTO(
                    outTradeNo,
                    String.valueOf(orderId),
                    APP_PAY_CHANNEL,
                    response.getBody(),
                    RESPONSE_TIME_FORMATTER.format(expiresAt));
        } catch (AlipayApiException e) {
            throw new ClientException("Alipay app payment request failed", e, BaseErrorCode.SERVICE_ERROR);
        }
    }

    @Transactional
    public boolean handleAlipayNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }
        String tradeStatus = params.get("trade_status");
        if (!Objects.equals(tradeStatus, "TRADE_SUCCESS") && !Objects.equals(tradeStatus, "TRADE_FINISHED")) {
            return true;
        }
        Long orderId = parseOrderId(params.get("out_trade_no"));
        if (orderId == null) {
            return false;
        }
        OrderDO order = requireOrder(orderId);
        if (!amountMatches(order.getTotalAmount(), params.get("total_amount"))) {
            return false;
        }
        if (Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_FULFILL)) {
            return true;
        }
        if (!Objects.equals(order.getStatus(), ORDER_STATUS_PENDING_PAY)) {
            return false;
        }
        orderSettlementService.markPaidAndCreateEscrow(orderId);
        return true;
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    private boolean amountMatches(BigDecimal expected, String actualValue) {
        if (expected == null || isBlank(actualValue)) {
            return false;
        }
        try {
            BigDecimal actual = new BigDecimal(actualValue).setScale(2, RoundingMode.DOWN);
            return expected.setScale(2, RoundingMode.DOWN).compareTo(actual) == 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String buildPaymentId(Long orderId) {
        String timestamp = LocalDateTime.now().format(PAYMENT_ID_TIME_FORMATTER);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORDER_" + orderId + "_" + timestamp + "_" + random;
    }

    private Long parseOrderId(String paymentId) {
        if (isBlank(paymentId) || !paymentId.startsWith("ORDER_")) {
            return null;
        }
        String[] parts = paymentId.split("_");
        if (parts.length < 2) {
            return null;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, RoundingMode.DOWN).toPlainString();
    }

    private String resolvePageReturnUrl(OrderAlipayPaymentReqDTO reqDTO) {
        if (reqDTO != null && !isBlank(reqDTO.returnUrl())) {
            return reqDTO.returnUrl();
        }
        return alipayProperties.getReturnUrl();
    }

    private String resolvePageQuitUrl(OrderAlipayPaymentReqDTO reqDTO) {
        if (reqDTO != null && !isBlank(reqDTO.quitUrl())) {
            return reqDTO.quitUrl();
        }
        if (!isBlank(alipayProperties.getPageQuitUrl())) {
            return alipayProperties.getPageQuitUrl();
        }
        return alipayProperties.getReturnUrl();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
