package com.example.pets_backend.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.example.pets_backend.config.AlipayProperties;
import com.example.pets_backend.dto.req.WalletRechargeConfirmReqDTO;
import com.example.pets_backend.dto.req.WalletRechargeReqDTO;
import com.example.pets_backend.dto.req.WalletWithdrawReqDTO;
import com.example.pets_backend.dto.resp.CaretakerWalletBalanceRespDTO;
import com.example.pets_backend.dto.req.MaterialProgressReqDTO;
import com.example.pets_backend.dto.req.SubmitExamReqDTO;
import com.example.pets_backend.dto.resp.TrainingCurriculumRespDTO;
import com.example.pets_backend.dto.resp.WalletRechargeConfirmRespDTO;
import com.example.pets_backend.dto.resp.WalletRechargeRespDTO;
import com.example.pets_backend.dto.resp.WalletWithdrawRespDTO;
import com.example.pets_backend.dto.resp.StartExamRespDTO;
import com.example.pets_backend.dto.resp.SubmitExamRespDTO;
import com.example.pets_backend.dto.resp.TempCaretakerReadyRespDTO;
import com.example.pets_backend.dto.resp.TrainingMaterialRespDTO;
import com.example.pets_backend.dto.resp.TrainingStatusRespDTO;
import com.example.pets_backend.dto.resp.UpgradeCaretakerRoleRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.WalletService;
import com.example.pets_backend.service.TrainingService;
import com.example.pets_backend.service.UserRoleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final WalletService walletService;
    private final AlipayProperties alipayProperties;
    private final TrainingService trainingService;
    private final UserRoleService userRoleService;

    @GetMapping("/wallet")
    public Result<CaretakerWalletBalanceRespDTO> wallet() {
        return Results.success(walletService.getCurrentUserBalance());
    }

    @PostMapping("/wallet/recharge")
    public Result<WalletRechargeRespDTO> recharge(@RequestBody WalletRechargeReqDTO reqDTO) {
        return Results.success(walletService.createRecharge(reqDTO));
    }

    @PostMapping("/wallet/recharge/confirm")
    public Result<WalletRechargeConfirmRespDTO> confirmRecharge(
            @RequestBody WalletRechargeConfirmReqDTO reqDTO) {
        String outTradeNo = reqDTO == null ? null : reqDTO.outTradeNo();
        return Results.success(walletService.confirmRecharge(outTradeNo));
    }

    @PostMapping("/wallet/withdraw")
    public Result<WalletWithdrawRespDTO> withdraw(@RequestBody WalletWithdrawReqDTO reqDTO) {
        return Results.success(walletService.withdraw(reqDTO));
    }

    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) throws Exception {
        Map<String, String> params = extractParams(request);
        boolean verified = AlipaySignature.rsaCheckV1(
                params,
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getCharset(),
                alipayProperties.getSignType());
        if (!verified) {
            return "fail";
        }
        walletService.handleRechargeNotify(params);
        return "success";
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }
        return params;
    }

    @PostMapping("/training/curriculum")
    public Result<TrainingCurriculumRespDTO> trainingCurriculum() {
        return Results.success(trainingService.getCurriculum());
    }

    @PostMapping("/training/materials/{materialId}/progress")
    public Result<TrainingCurriculumRespDTO> recordMaterialProgress(
            @org.springframework.web.bind.annotation.PathVariable Long materialId,
            @RequestBody MaterialProgressReqDTO reqDTO) {
        return Results.success(trainingService.recordMaterialProgress(materialId, reqDTO));
    }

    @PostMapping("/training/start")
    public Result<TrainingMaterialRespDTO> startTraining() {
        return Results.success(trainingService.startLearning());
    }

    @PostMapping("/training/complete")
    public Result<Void> completeTraining() {
        trainingService.completeLearning();
        return Results.success();
    }

    @PostMapping("/training/exam/start")
    public Result<StartExamRespDTO> startExam() {
        return Results.success(trainingService.startExam());
    }

    @PostMapping("/training/exam/submit")
    public Result<SubmitExamRespDTO> submitExam(@RequestBody SubmitExamReqDTO reqDTO) {
        return Results.success(trainingService.submitExam(reqDTO));
    }

    @PostMapping("/training/status")
    public Result<TrainingStatusRespDTO> trainingStatus() {
        return Results.success(trainingService.getStatus());
    }

    @PostMapping("/training/pass-temp")
    public Result<TempCaretakerReadyRespDTO> passTrainingTemporarily() {
        return Results.success(trainingService.passTrainingTemporarily());
    }

    @PostMapping("/role/upgrade-to-caretaker-temp")
    public Result<UpgradeCaretakerRoleRespDTO> upgradeToCaretakerTemporarily() {
        return Results.success(userRoleService.upgradeCurrentUserToCaretakerTemporarily());
    }
}
