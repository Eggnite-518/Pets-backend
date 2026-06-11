package com.example.pets_backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.pets_backend.dao.mapper")
public class MyBatisConfig {
}
