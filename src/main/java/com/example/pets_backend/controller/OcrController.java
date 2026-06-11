package com.example.pets_backend.controller;

import com.example.pets_backend.dto.resp.IdCardOcrRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/id-card")
    public Result<IdCardOcrRespDTO> recognizeIdCard(@RequestParam MultipartFile file,
                                                     @RequestParam(required = false, defaultValue = "face") String side) {
        return Results.success(ocrService.recognizeIdCard(file, side));
    }
}
