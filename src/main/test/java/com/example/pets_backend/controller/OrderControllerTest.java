package com.example.pets_backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.pets_backend.dto.resp.ApplicationBriefRespDTO;
import com.example.pets_backend.dto.resp.CreateOrderRespDTO;
import com.example.pets_backend.dto.resp.DisputeRespDTO;
import com.example.pets_backend.dto.resp.MyRewardingOrderRespDTO;
import com.example.pets_backend.dto.resp.OpenOrderPageRespDTO;
import com.example.pets_backend.dto.resp.OpenOrderPetDTO;
import com.example.pets_backend.dto.resp.OpenOrderRespDTO;
import com.example.pets_backend.dto.resp.OrderAlipayAppPaymentRespDTO;
import com.example.pets_backend.dto.resp.OrderAlipayPaymentRespDTO;
import com.example.pets_backend.dto.resp.OrderApplicationRespDTO;
import com.example.pets_backend.dto.resp.OrderPetBriefRespDTO;
import com.example.pets_backend.dto.resp.OrderSettlementRespDTO;
import com.example.pets_backend.dto.resp.CandidateListItemRespDTO;
import com.example.pets_backend.dto.resp.CandidateListRespDTO;
import com.example.pets_backend.dto.resp.ProviderDetailRespDTO;
import com.example.pets_backend.dto.resp.ServiceItemRespDTO;
import com.example.pets_backend.dto.resp.SubmitDisputeRespDTO;
import com.example.pets_backend.frameworks.web.GlobalExceptionHandler;
import com.example.pets_backend.service.FulfillmentProtectionService;
import com.example.pets_backend.service.OrderCandidateService;
import com.example.pets_backend.service.OrderApplicationService;
import com.example.pets_backend.service.OrderDisputeService;
import com.example.pets_backend.service.OrderPaymentService;
import com.example.pets_backend.service.OrderRatingService;
import com.example.pets_backend.service.OrderService;
import com.example.pets_backend.service.OrderSettlementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderApplicationService orderApplicationService;

    @Mock
    private OrderCandidateService orderCandidateService;

    @Mock
    private OrderService orderService;

    @Mock
    private FulfillmentProtectionService fulfillmentProtectionService;

    @Mock
    private OrderRatingService orderRatingService;

    @Mock
    private OrderDisputeService orderDisputeService;

    @Mock
    private OrderPaymentService orderPaymentService;

    @Mock
    private OrderSettlementService orderSettlementService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderApplicationService, orderCandidateService, orderService,
                        fulfillmentProtectionService, orderRatingService, orderDisputeService, orderPaymentService,
                        orderSettlementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void applyReturnsUnifiedResponse() throws Exception {
        when(orderApplicationService.apply(any())).thenReturn(new OrderApplicationRespDTO(4004L));

        mockMvc.perform(post("/api/v1/orders/2002/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.applicationId", is(4004)));
    }

    @Test
    void createOrderAcceptsExtendedPayload() throws Exception {
        when(orderService.create(any())).thenReturn(new CreateOrderRespDTO(
                2002L,
                new BigDecimal("80.00"),
                1,
                "悬赏中",
                "2026-05-24 12:30:45",
                List.of(),
                List.of()));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "petIds": ["3001", "3002"],
                                  "serviceType": 1,
                                  "addressId": "5001",
                                  "serviceDate": "2026-05-25",
                                  "serviceStartTime": "14:00",
                                  "serviceEndTime": "15:00",
                                  "finalAmount": "80.00",
                                  "remark": "猫粮在厨房柜子里，请换新水并拍照反馈"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.orderId", is(2002)))
                .andExpect(jsonPath("$.data.totalAmount", is(80.00)))
                .andExpect(jsonPath("$.data.createdAt", is("2026-05-24 12:30:45")));
    }

    @Test
    void cancelReturnsSuccess() throws Exception {
        doNothing().when(orderApplicationService).cancel(anyLong());

        mockMvc.perform(delete("/api/v1/orders/2002/reservations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }

    @Test
    void listMyRewardingReturnsUnifiedResponse() throws Exception {
        when(orderService.listMyRewarding()).thenReturn(List.of(
                new MyRewardingOrderRespDTO(
                        2002L,
                        LocalDate.of(2026, 5, 3),
                        BigDecimal.valueOf(98),
                        "address snapshot",
                        1,
                        List.of(new OrderPetBriefRespDTO(3001L, "pet", 1)),
                        List.of(new ApplicationBriefRespDTO(4002L, 1002L, "provider",
                                "https://example.com/avatar/1002.png", 0))
                )));

        mockMvc.perform(get("/api/v1/orders/my/rewarding")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data[0].orderId", is(2002)))
                .andExpect(jsonPath("$.data[0].pets[0].petName", is("pet")))
                .andExpect(jsonPath("$.data[0].applications[0].providerNickname", is("provider")));
    }

    @Test
    void listCandidatesReturnsSortedList() throws Exception {
        when(orderCandidateService.listCandidates(anyLong(), any())).thenReturn(new CandidateListRespDTO(
                2002L,
                "distance",
                "距离最近",
                List.of(new CandidateListItemRespDTO(
                        4002L,
                        1002L,
                        "provider",
                        "https://example.com/avatar.png",
                        0,
                        "报名中",
                        0.8,
                        4.8,
                        20,
                        90))));

        mockMvc.perform(get("/api/v1/orders/2002/reservations?sortBy=distance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.sortBy", is("distance")))
                .andExpect(jsonPath("$.data.candidates[0].providerId", is(1002)))
                .andExpect(jsonPath("$.data.candidates[0].distanceKm", is(0.8)))
                .andExpect(jsonPath("$.data.candidates[0].rating", is(4.8)));
    }

    @Test
    void getProviderDetailReturnsUnifiedResponse() throws Exception {
        when(orderCandidateService.getProviderDetail(anyLong(), anyLong())).thenReturn(
                new ProviderDetailRespDTO(
                        4002L,
                        2002L,
                        1,
                        "waiting",
                        List.of(new ServiceItemRespDTO(1, "feeding")),
                        98,
                        0.8,
                        "pet",
                        null,
                        "memo",
                        1002L,
                        "provider",
                        "https://example.com/avatar.png",
                        92,
                        4.8,
                        28,
                        96.5,
                        "银牌宠托师",
                        List.of("实名认证"),
                        15,
                        4.7,
                        4.9));

        mockMvc.perform(get("/api/v1/orders/2002/reservations/1002")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.applicationId", is(4002)))
                .andExpect(jsonPath("$.data.orderStatusText", is("waiting")))
                .andExpect(jsonPath("$.data.serviceItems[0].serviceTypeText", is("feeding")))
                .andExpect(jsonPath("$.data.creditScore", is(92)))
                .andExpect(jsonPath("$.data.complianceRate", is(96.5)));
    }

    @Test
    void listOpenOrdersReturnsPagedResponse() throws Exception {
        when(orderService.listOpenOrders(any(), anyInt(), anyInt())).thenReturn(
                new OpenOrderPageRespDTO(
                        1,
                        1,
                        10,
                        List.of(new OpenOrderRespDTO(
                                2002L,
                                List.of(new ServiceItemRespDTO(1, "feeding")),
                                98,
                                "2026-05-03",
                                null,
                                "district",
                                0.0,
                                2,
                                false,
                                List.of(new OpenOrderPetDTO("pet", 1, null)),
                                null,
                                List.of(),
                                List.of()))));

        mockMvc.perform(get("/api/v1/orders/open?page=1&pageSize=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.total", is(1)))
                .andExpect(jsonPath("$.data.list[0].orderId", is(2002)));
    }

    @Test
    void selectProviderReturnsSuccess() throws Exception {
        doNothing().when(orderService).selectProvider(anyLong(), anyLong());

        mockMvc.perform(post("/api/v1/orders/2002/reservations/1002/selection")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }

    @Test
    void payOrderReturnsSuccess() throws Exception {
        doNothing().when(orderService).payOrder(anyLong());

        mockMvc.perform(post("/api/v1/orders/2002/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }

    @Test
    void createAlipayPaymentReturnsPaymentPayload() throws Exception {
        when(orderPaymentService.createAlipayPayment(anyLong(), any()))
                .thenReturn(new OrderAlipayPaymentRespDTO(
                        "ORDER_2002_20260602120000_abcd1234",
                        "2002",
                        "ALIPAY_PAGE",
                        "<form>pay</form>",
                        "2026-06-02 12:30:00"));

        mockMvc.perform(post("/api/v1/orders/2002/payments/alipay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "returnUrl": "https://api.itsbrain.me/pay-return",
                                  "quitUrl": "https://api.itsbrain.me/pay-quit"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.paymentId", is("ORDER_2002_20260602120000_abcd1234")))
                .andExpect(jsonPath("$.data.payChannel", is("ALIPAY_PAGE")));
    }

    @Test
    void createAlipayPagePaymentReturnsPaymentPayload() throws Exception {
        when(orderPaymentService.createAlipayPagePayment(anyLong(), any()))
                .thenReturn(new OrderAlipayPaymentRespDTO(
                        "ORDER_2002_20260602120000_abcd1234",
                        "2002",
                        "ALIPAY_PAGE",
                        "<form>pay</form>",
                        "2026-06-02 12:30:00"));

        mockMvc.perform(post("/api/v1/orders/2002/payments/alipay/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "returnUrl": "https://api.itsbrain.me/pay-return",
                                  "quitUrl": "https://api.itsbrain.me/pay-quit"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.paymentId", is("ORDER_2002_20260602120000_abcd1234")))
                .andExpect(jsonPath("$.data.payChannel", is("ALIPAY_PAGE")));
    }

    @Test
    void createAlipayAppPaymentReturnsOrderString() throws Exception {
        when(orderPaymentService.createAlipayAppPayment(anyLong()))
                .thenReturn(new OrderAlipayAppPaymentRespDTO(
                        "ORDER_2002_20260602120000_abcd1234",
                        "2002",
                        "ALIPAY_APP",
                        "app_id=xxx&biz_content=yyy&sign=zzz",
                        "2026-06-02 12:30:00"));

        mockMvc.perform(post("/api/v1/orders/2002/payments/alipay/app")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.paymentId", is("ORDER_2002_20260602120000_abcd1234")))
                .andExpect(jsonPath("$.data.payChannel", is("ALIPAY_APP")))
                .andExpect(jsonPath("$.data.orderStr", is("app_id=xxx&biz_content=yyy&sign=zzz")));
    }

    @Test
    void confirmResolvedReturnsSuccess() throws Exception {
        doNothing().when(fulfillmentProtectionService).ownerConfirmResolved(anyLong());

        mockMvc.perform(post("/api/v1/orders/2002/exception/confirm-resolved")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }

    @Test
    void confirmCompletionReturnsSettlement() throws Exception {
        when(orderSettlementService.ownerConfirmCompletion(anyLong()))
                .thenReturn(new OrderSettlementRespDTO(
                        9001L, 2002L, 1001L, 1002L, "80.00", "20.00", "16.00", "64.00", 1, "托管中",
                        "2026-06-02 12:30:00"));

        mockMvc.perform(post("/api/v1/orders/2002/completion-confirmation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.orderId", is(2002)));
    }

    @Test
    void getSettlementReturnsSettlement() throws Exception {
        when(orderSettlementService.getSettlement(anyLong()))
                .thenReturn(new OrderSettlementRespDTO(
                        9001L, 2002L, 1001L, 1002L, "80.00", "20.00", "16.00", "64.00", 1, "托管中",
                        "2026-06-02 12:30:00"));

        mockMvc.perform(get("/api/v1/orders/2002/settlement")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.orderId", is(2002)))
                .andExpect(jsonPath("$.data.settlementStatusDesc", is("托管中")));
    }

    @Test
    void submitRatingReturnsSuccess() throws Exception {
        doNothing().when(orderRatingService).submitRating(anyLong(), any());

        mockMvc.perform(post("/api/v1/orders/2002/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5,\"comment\":\"good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.message", is("success")));
    }

    @Test
    void submitDisputeReturnsDisputeId() throws Exception {
        when(orderDisputeService.submitDispute(anyLong(), any()))
                .thenReturn(new SubmitDisputeRespDTO(7001L, 0));

        mockMvc.perform(post("/api/v1/orders/2002/disputes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"disputeType\":1,\"reason\":\"service dispute\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data.disputeId", is(7001)))
                .andExpect(jsonPath("$.data.disputeStatus", is(0)));
    }

    @Test
    void listDisputesReturnsDisputeStatus() throws Exception {
        when(orderDisputeService.listDisputes(anyLong())).thenReturn(List.of(
                new DisputeRespDTO(7001L, 2002L, 1001L, 1002L, 1, "service dispute",
                        List.of("https://example.com/a.jpg"), 0, null, null,
                        "2026-06-02 10:00:00", null)));

        mockMvc.perform(get("/api/v1/orders/2002/disputes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("0")))
                .andExpect(jsonPath("$.data[0].disputeId", is(7001)))
                .andExpect(jsonPath("$.data[0].disputeStatus", is(0)));
    }
}
