package com.example.pets_backend.frameworks.database.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.pets_backend.frameworks.database.handler.MyMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库框架自动装配
 */
@Configuration
public class DatabaseAutoConfiguration {

    /**
     * 注册元数据自动填充处理器
     *
     * @return 元数据处理器
     */
    @Bean
    public MetaObjectHandler myMetaObjectHandler() {
        return new MyMetaObjectHandler();
    }
}
