package com.example.pets_backend.service;

import com.example.pets_backend.dto.resp.ImageUploadRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final ObjectStorageService objectStorageService;
    private final OssAccessibleUrlService ossAccessibleUrlService;

    public ImageUploadRespDTO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = UserContext.getUserId();
        // 优先使用前端传来的 content-type，若缺失则按扩展名推断
        String contentType = resolveContentType(file);
        String objectKey = buildObjectKey(userId, file.getOriginalFilename(), contentType);
        try {
            OssUploadResult result = objectStorageService.upload(new OssUploadCommand(
                    objectKey,
                    file.getInputStream(),
                    file.getSize(),
                    contentType));
            // 私有 Bucket：返回预签名 URL 供前端立即展示
            return new ImageUploadRespDTO(ossAccessibleUrlService.toDisplayUrl(objectKey));
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    /** 从 Content-Type 或文件扩展名推断 MIME 类型，保证不为 null */
    private String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null && !ct.isBlank() && !ct.equalsIgnoreCase("application/octet-stream")) {
            return ct;
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".png"))  return "image/png";
            if (lower.endsWith(".webp")) return "image/webp";
            if (lower.endsWith(".gif"))  return "image/gif";
            if (lower.endsWith(".heic") || lower.endsWith(".heif")) return "image/heic";
        }
        return "image/jpeg"; // 默认当作 JPEG
    }

    private String buildObjectKey(Long userId, String originalFilename, String contentType) {
        String extension = extractExtension(originalFilename, contentType);
        String prefix = userId != null ? "images/users/" + userId : "images/anonymous";
        return prefix + "/" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
    }

    private String extractExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot != -1) return originalFilename.substring(dot + 1).toLowerCase();
        }
        if (contentType != null) {
            return switch (contentType.toLowerCase()) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "image/gif" -> "gif";
                default -> "";
            };
        }
        return "";
    }
}
