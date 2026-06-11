package com.example.pets_backend.controller;

import com.example.pets_backend.dto.resp.ReviewAttachmentUploadRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.ReviewAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewAttachmentService reviewAttachmentService;

    @PostMapping(value = "/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ReviewAttachmentUploadRespDTO> uploadAttachment(@RequestParam("file") MultipartFile file) {
        return Results.success(reviewAttachmentService.uploadAttachment(file));
    }
}
