package com.example.pets_backend.service;

import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.enums.TrainingResetReasonEnum;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingResetScheduler {

    private final OrderDao orderDao;
    private final TrainingService trainingService;

    @Scheduled(cron = "0 10 2 * * ?")
    public void resetInactiveProviders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(180);
        List<Long> providerIds = orderDao.selectInactiveProviders(cutoff);
        for (Long providerId : providerIds) {
            trainingService.resetTraining(providerId, TrainingResetReasonEnum.INACTIVE_180_DAYS.getCode());
        }
    }
}

