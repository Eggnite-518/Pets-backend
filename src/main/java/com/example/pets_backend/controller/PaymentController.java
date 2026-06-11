package com.example.pets_backend.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.example.pets_backend.config.AlipayProperties;
import com.example.pets_backend.service.OrderPaymentService;
import com.example.pets_backend.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderPaymentService orderPaymentService;
    private final WalletService walletService;
    private final AlipayProperties alipayProperties;

    @GetMapping(value = "/wallet/page-pay/{outTradeNo}", produces = MediaType.TEXT_HTML_VALUE)
    public String walletPagePay(@PathVariable String outTradeNo) {
        return walletService.getPayFormHtml(outTradeNo);
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
        if (isWalletRechargeNotification(params)) {
            walletService.handleRechargeNotify(params);
            return "success";
        }
        return orderPaymentService.handleAlipayNotify(params) ? "success" : "fail";
    }

    private boolean isWalletRechargeNotification(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        return outTradeNo != null && outTradeNo.startsWith("RC");
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
}
