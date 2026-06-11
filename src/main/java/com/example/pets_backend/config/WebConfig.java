package com.example.pets_backend.config;

import com.example.pets_backend.frameworks.auth.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 统一配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/send-code",
                        "/api/v1/auth/login-by-code",
                        "/api/v1/infra/ping",
                        "/api/v1/users/alipay/notify",
                        "/api/v1/payments/alipay/notify",
                        "/api/v1/payments/wallet/page-pay/**",
                        "/error");
    }
}
