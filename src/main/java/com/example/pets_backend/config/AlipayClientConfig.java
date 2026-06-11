package com.example.pets_backend.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayClientConfig {

    @Bean
    public AlipayClient alipayClient(AlipayProperties alipayProperties) {
        return new DefaultAlipayClient(
                alipayProperties.getServerUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType());
    }
}

