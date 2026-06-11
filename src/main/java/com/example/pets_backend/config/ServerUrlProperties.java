package com.example.pets_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application public server URL properties.
 */
@Component
@ConfigurationProperties(prefix = "pets.server")
public class ServerUrlProperties {

    private String publicBaseUrl = "http://localhost:8080";

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }
}
