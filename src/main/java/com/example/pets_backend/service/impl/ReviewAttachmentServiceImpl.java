package com.example.pets_backend.service.impl;

import com.example.pets_backend.dto.resp.ReviewAttachmentUploadRespDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import com.example.pets_backend.service.ReviewAttachmentService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewAttachmentServiceImpl implements ReviewAttachmentService {

    private final ObjectStorageService objectStorageService;

    @Override
    public ReviewAttachmentUploadRespDTO uploadAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        String contentType = file.getContentType();
        String mediaType = resolveMediaType(contentType);
        String objectKey = buildObjectKey(file.getOriginalFilename());
        try {
            OssUploadResult result = objectStorageService.upload(new OssUploadCommand(
                    objectKey, file.getInputStream(), file.getSize(), contentType));
            return new ReviewAttachmentUploadRespDTO(
                    result.fileUrl(), result.objectKey(), mediaType, result.contentType(), result.fileSize());
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.OSS_UPLOAD_ERROR);
        }
    }

    private String resolveMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return "IMAGE";
        }
        if (contentType != null && contentType.startsWith("video/")) {
            return "VIDEO";
        }
        return "FILE";
    }

    private String buildObjectKey(String originalFilename) {
        LocalDate today = LocalDate.now();
        String suffix = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                suffix = originalFilename.substring(dotIndex);
            }
        }
        return "reviews/%d/%02d/%s%s".formatted(
                today.getYear(), today.getMonthValue(), UUID.randomUUID(), suffix);
    }
}
