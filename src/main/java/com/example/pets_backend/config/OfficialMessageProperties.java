package com.example.pets_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pets.official-message")
public class OfficialMessageProperties {

    private Long systemSenderId = 999999L;

    public Long getSystemSenderId() {
        return systemSenderId;
    }

    public void setSystemSenderId(Long systemSenderId) {
        this.systemSenderId = systemSenderId;
    }
}
