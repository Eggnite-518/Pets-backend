package com.example.pets_backend.controller;

import com.example.pets_backend.dto.resp.ImageUploadRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ImageUploadService imageUploadService;

    /**
     * 通用图片上传接口（头像、宠物档案图等）。
     * 需要登录（携带 JWT Token），返回 OSS 公开访问 URL。
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ImageUploadRespDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        return Results.success(imageUploadService.uploadImage(file));
    }
}
