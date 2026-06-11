package com.example.pets_backend.controller;

import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infra")
public class InfraController {

    @GetMapping("/ping")
    public Result<String> ping() {
        return Results.success("pong");
    }
}
