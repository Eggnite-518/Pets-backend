package com.example.pets_backend.service.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teaopenapi.models.Config;
import com.example.pets_backend.config.AliyunSmsProperties;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pets.sms.real-send", havingValue = "true")
public class AliyunSmsService implements SmsService {

    private final AliyunSmsProperties smsProperties;

    @Override
    public void sendCode(String phone, String code) {
        try {
            Config config = new Config()
                    .setAccessKeyId(smsProperties.getAccessKeyId())
                    .setAccessKeySecret(smsProperties.getAccessKeySecret());
            config.endpoint = "dypnsapi.aliyuncs.com";
            Client client = new Client(config);

            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(smsProperties.getTemplateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");

            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request);
            if (response.getBody() == null || !"OK".equals(response.getBody().getCode())) {
                log.error("SMS send failed: code={}, message={}",
                        response.getBody() != null ? response.getBody().getCode() : "null",
                        response.getBody() != null ? response.getBody().getMessage() : "null");
                throw new com.example.pets_backend.frameworks.convention.exception.ClientException(
                        BaseErrorCode.SERVICE_ERROR);
            }
            log.info("SMS sent to {}", phone);
        } catch (com.example.pets_backend.frameworks.convention.exception.ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("SMS send error", e);
            throw new com.example.pets_backend.frameworks.convention.exception.ClientException(
                    BaseErrorCode.SERVICE_ERROR);
        }
    }
}
