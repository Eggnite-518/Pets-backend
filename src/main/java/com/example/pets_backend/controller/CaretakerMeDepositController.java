package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.CaretakerDepositRechargeReqDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRechargeRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRefundApplyRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRefundRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRefundStatusRespDTO;
import com.example.pets_backend.dto.resp.CaretakerDepositRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.CaretakerDepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caretaker/me")
@RequiredArgsConstructor
public class CaretakerMeDepositController {

    private final CaretakerDepositService caretakerDepositService;

    @GetMapping("/deposit")
    public Result<CaretakerDepositRespDTO> getDeposit() {
        return Results.success(caretakerDepositService.getMyDeposit());
    }

    @PostMapping("/deposit/recharge")
    public Result<CaretakerDepositRechargeRespDTO> rechargeDeposit(
            @RequestBody(required = false) CaretakerDepositRechargeReqDTO reqDTO) {
        return Results.success(caretakerDepositService.recharge(reqDTO));
    }

    @PostMapping("/deposit/refund/apply")
    public Result<CaretakerDepositRefundApplyRespDTO> applyRefund() {
        return Results.success(caretakerDepositService.applyRefund());
    }

    @GetMapping("/deposit/refund/status")
    public Result<CaretakerDepositRefundStatusRespDTO> getRefundStatus() {
        return Results.success(caretakerDepositService.getRefundStatus());
    }

    @PostMapping("/deposit/refund/settle")
    public Result<CaretakerDepositRefundRespDTO> settleRefund() {
        return Results.success(caretakerDepositService.settleRefund());
    }
}
