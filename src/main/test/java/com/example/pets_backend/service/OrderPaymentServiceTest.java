package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private AlipayClient alipayClient;

    @Mock
    private AlipayTradePagePayResponse alipayPageResponse;

    @Mock
    private AlipayTradeAppPayResponse alipayAppResponse;

    @Mock
    private OrderSettlementService orderSettlementService;

    private OrderPaymentService orderPaymentService;

    @BeforeEach
    void setUp() {
        AlipayProperties properties = new AlipayProperties();
        properties.setNotifyUrl("https://api.itsbrain.me/api/v1/payments/alipay/notify");
        properties.setReturnUrl("https://api.itsbrain.me/pay-return");
        properties.setPageQuitUrl("https://api.itsbrain.me/pay-quit");
        orderPaymentService = new OrderPaymentService(orderDao, alipayClient, properties,
                new ObjectMapper(), orderSettlementService);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "owner", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createAlipayPaymentReturnsPaymentPayload() throws Exception {
        OrderDO order = buildOrder(2002L, 1001L, 2, BigDecimal.valueOf(98));
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(alipayClient.pageExecute(any(AlipayTradePagePayRequest.class))).thenReturn(alipayPageResponse);
        when(alipayPageResponse.isSuccess()).thenReturn(true);
        when(alipayPageResponse.getBody()).thenReturn("<form>pay</form>");

        OrderAlipayPaymentRespDTO result = orderPaymentService.createAlipayPayment(2002L,
                new OrderAlipayPaymentReqDTO("pets://orders/2002/pay-success", "pets://orders/2002"));

        assertTrue(result.paymentId().startsWith("ORDER_2002_"));
        assertEquals("2002", result.orderId());
        assertEquals("ALIPAY_PAGE", result.payChannel());
        assertEquals("<form>pay</form>", result.payUrl());
    }

    @Test
    void createAlipayPagePaymentUsesConfiguredFallbackUrls() throws Exception {
        OrderDO order = buildOrder(2002L, 1001L, 2, BigDecimal.valueOf(98));
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(alipayClient.pageExecute(any(AlipayTradePagePayRequest.class))).thenReturn(alipayPageResponse);
        when(alipayPageResponse.isSuccess()).thenReturn(true);
        when(alipayPageResponse.getBody()).thenReturn("<form>pay</form>");

        OrderAlipayPaymentRespDTO result = orderPaymentService.createAlipayPagePayment(2002L, null);

        assertEquals("ALIPAY_PAGE", result.payChannel());
        verify(alipayClient).pageExecute(argThat(request ->
                "https://api.itsbrain.me/pay-return".equals(request.getReturnUrl())));
    }

    @Test
    void createAlipayAppPaymentReturnsOrderString() throws Exception {
        OrderDO order = buildOrder(2002L, 1001L, 2, BigDecimal.valueOf(98));
        when(orderDao.selectById(2002L)).thenReturn(order);
        when(alipayClient.sdkExecute(any(AlipayTradeAppPayRequest.class))).thenReturn(alipayAppResponse);
        when(alipayAppResponse.isSuccess()).thenReturn(true);
        when(alipayAppResponse.getBody()).thenReturn("app_id=xxx&biz_content=yyy&sign=zzz");

        OrderAlipayAppPaymentRespDTO result = orderPaymentService.createAlipayAppPayment(2002L);

        assertTrue(result.paymentId().startsWith("ORDER_2002_"));
        assertEquals("2002", result.orderId());
        assertEquals("ALIPAY_APP", result.payChannel());
        assertEquals("app_id=xxx&biz_content=yyy&sign=zzz", result.orderStr());
    }

    @Test
    void handleAlipayNotifyUpdatesPendingPayOrder() {
        OrderDO order = buildOrder(2002L, 1001L, 2, BigDecimal.valueOf(98));
        when(orderDao.selectById(2002L)).thenReturn(order);

        boolean result = orderPaymentService.handleAlipayNotify(Map.of(
                "trade_status", "TRADE_SUCCESS",
                "out_trade_no", "ORDER_2002_20260602120000_abcd1234",
                "total_amount", "98.00"));

        assertTrue(result);
        verify(orderSettlementService).markPaidAndCreateEscrow(2002L);
    }

    private OrderDO buildOrder(Long orderId, Long ownerId, Integer status, BigDecimal amount) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(ownerId);
        order.setStatus(status);
        order.setTotalAmount(amount);
        return order;
    }
}
