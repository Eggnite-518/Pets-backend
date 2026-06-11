package com.example.pets_backend.service;

import com.example.pets_backend.config.ServerUrlProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provides application public server URL helpers.
 */
@Service
@RequiredArgsConstructor
public class ServerUrlService {

    private final ServerUrlProperties serverUrlProperties;

    public String getPublicBaseUrl() {
        return trimTrailingSlash(serverUrlProperties.getPublicBaseUrl());
    }

    public String buildPublicUrl(String path) {
        if (isBlank(path)) {
            return getPublicBaseUrl();
        }
        if (isAbsoluteUrl(path)) {
            return path;
        }
        return getPublicBaseUrl() + normalizePath(path);
    }

    private String trimTrailingSlash(String value) {
        if (isBlank(value)) {
            return "http://localhost:8080";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private boolean isAbsoluteUrl(String path) {
        return path.startsWith("http://") || path.startsWith("https://");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
