package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.CaretakerWalletWithdrawReqDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletBalanceRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletRecordsRespDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletWithdrawRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caretaker/me")
@RequiredArgsConstructor
public class CaretakerMeWalletController {

    private final WalletService walletService;

    @GetMapping("/wallet")
    public Result<CaretakerWalletBalanceRespDTO> getWalletBalance() {
        return Results.success(walletService.getCaretakerBalance());
    }

    @GetMapping("/wallet/withdraw")
    public Result<CaretakerWalletWithdrawRespDTO> applyWithdraw(
            @RequestBody CaretakerWalletWithdrawReqDTO reqDTO) {
        return Results.success(walletService.applyCaretakerWithdraw(reqDTO));
    }

    @GetMapping("/wallet/records")
    public Result<CaretakerWalletRecordsRespDTO> listWalletRecords(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Results.success(walletService.listCaretakerWalletRecords(page, pageSize));
    }
}
