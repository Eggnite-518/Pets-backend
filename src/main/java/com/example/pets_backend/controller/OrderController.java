package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.CreateOrderReqDTO;
import com.example.pets_backend.dto.req.OrderServiceFeeQuoteReqDTO;
import com.example.pets_backend.dto.req.OrderAlipayPaymentReqDTO;
import com.example.pets_backend.dto.req.SubmitDisputeReqDTO;
import com.example.pets_backend.dto.req.SubmitRatingReqDTO;
import com.example.pets_backend.dto.resp.CandidateListRespDTO;
import com.example.pets_backend.dto.resp.OrderAlipayAppPaymentRespDTO;
import com.example.pets_backend.dto.resp.CreateOrderRespDTO;
import com.example.pets_backend.dto.resp.DisputeRespDTO;
import com.example.pets_backend.dto.resp.EvidenceChainRespDTO;
import com.example.pets_backend.dto.resp.MyRewardingOrderRespDTO;
import com.example.pets_backend.dto.resp.OrderAlipayPaymentRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordsRespDTO;
import com.example.pets_backend.dto.resp.OrderDetailRespDTO;
import com.example.pets_backend.dto.resp.OrderRatingDetailRespDTO;
import com.example.pets_backend.dto.resp.OpenOrderPageRespDTO;
import com.example.pets_backend.dto.resp.OrderApplicationRespDTO;
import com.example.pets_backend.dto.resp.OrderSettlementRespDTO;
import com.example.pets_backend.dto.resp.OrderServiceFeeQuoteRespDTO;
import com.example.pets_backend.dto.resp.ProviderDetailRespDTO;
import com.example.pets_backend.dto.resp.ReorderPrefillRespDTO;
import com.example.pets_backend.dto.resp.SubmitDisputeRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.FulfillmentProtectionService;
import com.example.pets_backend.service.OrderCandidateService;
import com.example.pets_backend.service.OrderApplicationService;
import com.example.pets_backend.service.OrderDisputeService;
import com.example.pets_backend.service.OrderPaymentService;
import com.example.pets_backend.service.OrderRatingService;
import com.example.pets_backend.service.OrderSettlementService;
import com.example.pets_backend.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderCandidateService orderCandidateService;
    private final OrderService orderService;
    private final FulfillmentProtectionService fulfillmentProtectionService;
    private final OrderRatingService orderRatingService;
    private final OrderDisputeService orderDisputeService;
    private final OrderPaymentService orderPaymentService;
    private final OrderSettlementService orderSettlementService;

    @PostMapping
    public Result<CreateOrderRespDTO> create(@RequestBody CreateOrderReqDTO reqDTO) {
        return Results.success(orderService.create(reqDTO));
    }

    @PostMapping("/quote")
    public Result<OrderServiceFeeQuoteRespDTO> quote(@RequestBody OrderServiceFeeQuoteReqDTO reqDTO) {
        return Results.success(orderService.quote(reqDTO));
    }

    @PostMapping("/{orderId}/reservations")
    public Result<OrderApplicationRespDTO> apply(@PathVariable Long orderId) {
        return Results.success(orderApplicationService.apply(orderId));
    }

    @DeleteMapping("/{orderId}/reservations")
    public Result<Void> cancel(@PathVariable Long orderId) {
        orderApplicationService.cancel(orderId);
        return Results.success();
    }

    @GetMapping("/open")
    public Result<OpenOrderPageRespDTO> listOpenOrders(
            @RequestParam(required = false) Integer petType,
            @RequestParam(required = false) Integer serviceType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Results.success(orderService.listOpenOrders(petType, serviceType, page, pageSize));
    }

    @GetMapping("/my/rewarding")
    public Result<List<MyRewardingOrderRespDTO>> listMyRewarding() {
        return Results.success(orderService.listMyRewarding());
    }

    @GetMapping("/my")
    public Result<List<MyRewardingOrderRespDTO>> listMyOrders(@RequestParam(required = false) Integer status) {
        return Results.success(orderService.listMyOrders(status));
    }

    @GetMapping("/my/open")
    public Result<List<MyRewardingOrderRespDTO>> listMyOpen() {
        return Results.success(orderService.listMyOpen());
    }

    @GetMapping("/{orderId}/reorder-prefill")
    public Result<ReorderPrefillRespDTO> reorderPrefill(@PathVariable Long orderId) {
        return Results.success(orderService.reorderPrefill(orderId));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDetailRespDTO> getOrderDetail(@PathVariable Long orderId) {
        return Results.success(orderService.getOrderDetail(orderId));
    }

    @GetMapping("/{orderId}/fulfillment-records")
    public Result<FulfillmentRecordsRespDTO> listFulfillmentRecords(@PathVariable Long orderId) {
        return Results.success(orderService.listFulfillmentRecords(orderId));
    }

    @GetMapping("/{orderId}/reservations")
    public Result<CandidateListRespDTO> listCandidates(@PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "distance") String sortBy) {
        return Results.success(orderCandidateService.listCandidates(orderId, sortBy));
    }

    @GetMapping("/{orderId}/reservations/{providerId}")
    public Result<ProviderDetailRespDTO> getProviderDetail(@PathVariable Long orderId,
            @PathVariable Long providerId) {
        return Results.success(orderCandidateService.getProviderDetail(orderId, providerId));
    }

    @PostMapping("/{orderId}/reservations/{providerId}/selection")
    public Result<Void> selectProvider(@PathVariable Long orderId,
            @PathVariable Long providerId) {
        orderService.selectProvider(orderId, providerId);
        return Results.success();
    }

    @PostMapping("/{orderId}/payments")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
        return Results.success();
    }

    @PostMapping("/{orderId}/payments/alipay")
    public Result<OrderAlipayPaymentRespDTO> createAlipayPayment(@PathVariable Long orderId,
            @RequestBody OrderAlipayPaymentReqDTO reqDTO) {
        return Results.success(orderPaymentService.createAlipayPayment(orderId, reqDTO));
    }

    @PostMapping("/{orderId}/payments/alipay/page")
    public Result<OrderAlipayPaymentRespDTO> createAlipayPagePayment(@PathVariable Long orderId,
            @RequestBody(required = false) OrderAlipayPaymentReqDTO reqDTO) {
        return Results.success(orderPaymentService.createAlipayPagePayment(orderId, reqDTO));
    }

    @PostMapping("/{orderId}/payments/alipay/app")
    public Result<OrderAlipayAppPaymentRespDTO> createAlipayAppPayment(@PathVariable Long orderId) {
        return Results.success(orderPaymentService.createAlipayAppPayment(orderId));
    }

    @PostMapping("/{orderId}/exception/confirm-resolved")
    public Result<Void> confirmResolved(@PathVariable Long orderId) {
        fulfillmentProtectionService.ownerConfirmResolved(orderId);
        return Results.success();
    }

    @PostMapping("/{orderId}/completion-confirmation")
    public Result<OrderSettlementRespDTO> confirmCompletion(@PathVariable Long orderId) {
        return Results.success(orderSettlementService.ownerConfirmCompletion(orderId));
    }

    @GetMapping("/{orderId}/settlement")
    public Result<OrderSettlementRespDTO> getSettlement(@PathVariable Long orderId) {
        return Results.success(orderSettlementService.getSettlement(orderId));
    }

    @PostMapping("/{orderId}/rating")
    public Result<Void> submitRating(@PathVariable Long orderId, @RequestBody SubmitRatingReqDTO reqDTO) {
        orderRatingService.submitRating(orderId, reqDTO);
        return Results.success();
    }

    @GetMapping("/{orderId}/rating")
    public Result<OrderRatingDetailRespDTO> getRating(@PathVariable Long orderId) {
        return Results.success(orderRatingService.getRating(orderId));
    }

    @PostMapping("/{orderId}/disputes")
    public Result<SubmitDisputeRespDTO> submitDispute(@PathVariable Long orderId,
            @RequestBody SubmitDisputeReqDTO reqDTO) {
        return Results.success(orderDisputeService.submitDispute(orderId, reqDTO));
    }

    @GetMapping("/{orderId}/disputes")
    public Result<List<DisputeRespDTO>> listDisputes(@PathVariable Long orderId) {
        return Results.success(orderDisputeService.listDisputes(orderId));
    }

    @GetMapping("/{orderId}/evidence")
    public Result<EvidenceChainRespDTO> getEvidence(@PathVariable Long orderId) {
        return Results.success(orderDisputeService.getEvidenceChain(orderId));
    }
}
