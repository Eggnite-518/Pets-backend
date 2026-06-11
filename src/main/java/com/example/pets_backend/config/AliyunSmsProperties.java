package com.example.pets_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pets.sms")
public class AliyunSmsProperties {

    private boolean realSend;
    private String signName;
    private String templateCode;
    private String accessKeyId;
    private String accessKeySecret;

    public boolean isRealSend() { return realSend; }
    public void setRealSend(boolean realSend) { this.realSend = realSend; }

    public String getSignName() { return signName; }
    public void setSignName(String signName) { this.signName = signName; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }

    public String getAccessKeySecret() { return accessKeySecret; }
    public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }
}
