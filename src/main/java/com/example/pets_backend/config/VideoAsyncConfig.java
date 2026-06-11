package com.example.pets_backend.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Local video async execution configuration.
 */
@Configuration
public class VideoAsyncConfig {

    /**
     * Creates the local executor used by video processing tasks.
     *
     * @return video processing executor
     */
    @Bean("videoTaskExecutor")
    public Executor videoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("video-task-");
        executor.initialize();
        return executor;
    }
}
