package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.SelfReportExceptionReqDTO;
import com.example.pets_backend.dto.resp.CaretakerOrderDetailRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentRecordsRespDTO;
import com.example.pets_backend.dto.resp.FulfillmentUploadRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.CaretakerOrderService;
import com.example.pets_backend.service.FulfillmentProtectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/caretaker/orders")
@RequiredArgsConstructor
public class CaretakerOrderController {

        private final CaretakerOrderService caretakerOrderService;
        private final FulfillmentProtectionService fulfillmentProtectionService;

    @PostMapping("/{orderId}/fulfillment")
    public Result<FulfillmentUploadRespDTO> uploadFulfillment(@PathVariable Long orderId,
            @RequestParam Integer nodeType,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        return Results.success(caretakerOrderService.uploadFulfillment(orderId, nodeType, file, lat, lng));
    }

    @GetMapping("/{orderId}")
    public Result<CaretakerOrderDetailRespDTO> getOrderDetail(@PathVariable Long orderId) {
        return Results.success(caretakerOrderService.getOrderDetail(orderId));
    }

        @GetMapping("/{orderId}/fulfillment-records")
        public Result<FulfillmentRecordsRespDTO> listFulfillmentRecords(@PathVariable Long orderId) {
                return Results.success(caretakerOrderService.listFulfillmentRecords(orderId));
        }

        @PostMapping("/{orderId}/exception/self-report")
        public Result<Void> selfReportException(@PathVariable Long orderId,
                        @RequestBody SelfReportExceptionReqDTO reqDTO) {
                fulfillmentProtectionService.selfReportException(orderId, reqDTO);
                return Results.success();
        }

        @PostMapping("/{orderId}/exception/no-fault-retreat")
        public Result<Void> noFaultRetreat(@PathVariable Long orderId) {
                fulfillmentProtectionService.noFaultRetreat(orderId);
                return Results.success();
        }
}
