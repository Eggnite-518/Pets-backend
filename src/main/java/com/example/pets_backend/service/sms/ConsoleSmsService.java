package com.example.pets_backend.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "pets.sms.real-send", havingValue = "false", matchIfMissing = true)
public class ConsoleSmsService implements SmsService {

    @Override
    public void sendCode(String phone, String code) {
        log.info("========== SMS VERIFICATION CODE ==========");
        log.info("  Phone: {}", phone);
        log.info("  Code:  {}", code);
        log.info("  Valid for 5 minutes");
        log.info("===========================================");
    }
}
