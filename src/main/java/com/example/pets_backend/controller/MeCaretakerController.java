package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.CaretakerProfileUpdateReqDTO;
import com.example.pets_backend.dto.resp.CaretakerProfileRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.CaretakerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/caretaker")
@RequiredArgsConstructor
public class MeCaretakerController {

    private final CaretakerProfileService caretakerProfileService;

    @GetMapping
    public Result<CaretakerProfileRespDTO> detail() {
        return Results.success(caretakerProfileService.getMyProfile());
    }

    @PutMapping
    public Result<CaretakerProfileRespDTO> update(@RequestBody CaretakerProfileUpdateReqDTO reqDTO) {
        return Results.success(caretakerProfileService.updateMyProfile(reqDTO));
    }
}

