package com.example.pets_backend.service;

import com.example.pets_backend.config.FulfillmentProtectionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FulfillmentProtectionScheduler {

    private final FulfillmentProtectionService fulfillmentProtectionService;
    private final FulfillmentProtectionProperties protectionProperties;

    @Scheduled(cron = "${pets.fulfillment-protection.scan-cron:0 */5 * * * ?}")
    public void scanRiskOrders() {
        log.debug("Run fulfillment protection scan with cron={}", protectionProperties.getScanCron());
        fulfillmentProtectionService.scanAndProtectOrders();
    }
}

