package com.example.pets_backend.service;

import com.example.pets_backend.config.AliyunOssProperties;
import com.example.pets_backend.config.VideoUploadProperties;
import com.example.pets_backend.dao.entity.FulfillmentRecordDO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FulfillmentMediaStorageService {

    private final ObjectStorageService objectStorageService;
    private final AliyunOssProperties aliyunOssProperties;
    private final VideoUploadProperties videoUploadProperties;
    private final VideoUploadService videoUploadService;

    public StoredMedia storeUploadedMedia(Long orderId, Integer nodeType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return StoredMedia.empty();
        }
        if (!isOssConfigured()) {
            return new StoredMedia(null, storeFileLocally(orderId, nodeType, file), file.getSize(), file.getContentType());
        }

        String objectKey = buildNodeObjectKey(orderId, nodeType, file.getOriginalFilename());
        try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            OssUploadResult uploadResult = objectStorageService.upload(new OssUploadCommand(
                    objectKey, inputStream, file.getSize(), file.getContentType()));
            return new StoredMedia(uploadResult.objectKey(), uploadResult.fileUrl(), uploadResult.fileSize(),
                    uploadResult.contentType());
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    public Path saveTempSourceFile(Long orderId, Integer nodeType, MultipartFile file) {
        Path tempDirectory = buildTempDirectory(orderId, nodeType);
        String extension = videoUploadService.getExtension(file.getOriginalFilename());
        Path sourceFile = tempDirectory.resolve(UUID.randomUUID() + "." + extension);
        try {
            Files.createDirectories(tempDirectory);
            file.transferTo(sourceFile);
            return sourceFile;
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.VIDEO_TEMP_FILE_ERROR);
        }
    }

    public String buildOriginalObjectKey(Long orderId, Integer nodeType, String originalFilename) {
        String extension = videoUploadService.getExtension(originalFilename);
        return "fulfillment/orders/" + orderId + "/nodes/" + nodeType + "/original/"
                + UUID.randomUUID() + "." + extension;
    }

    public String resolveAccessibleUrl(FulfillmentRecordDO record) {
        if (record.getObjectKey() != null && !record.getObjectKey().isBlank()) {
            return toHttpsUrl(objectStorageService.generatePresignedUrl(record.getObjectKey()));
        }
        return toHttpsUrl(record.getImageUrl());
    }

    private String toHttpsUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        if (url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }

    private String buildNodeObjectKey(Long orderId, Integer nodeType, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "fulfillment/orders/" + orderId + "/nodes/" + nodeType
                + "/" + UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
    }

    private Path buildTempDirectory(Long orderId, Integer nodeType) {
        String tempDir = videoUploadProperties.getTempDir();
        Path root = tempDir == null || tempDir.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "pets-video")
                : Path.of(tempDir);
        return root.resolve("orders").resolve(String.valueOf(orderId)).resolve("nodes")
                .resolve(String.valueOf(nodeType));
    }

    private String storeFileLocally(Long orderId, Integer nodeType, MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        String filename = "order_" + orderId + "_node_" + nodeType + "_" + UUID.randomUUID()
                + (extension.isEmpty() ? "" : "." + extension);
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "pets-fulfillment");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return target.toString();
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private boolean isOssConfigured() {
        return !isBlank(aliyunOssProperties.getEndpoint())
                && !isBlank(aliyunOssProperties.getBucketName())
                && !isBlank(aliyunOssProperties.getAccessKeyId())
                && !isBlank(aliyunOssProperties.getAccessKeySecret());
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot == -1 ? "" : originalFilename.substring(dot + 1);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record StoredMedia(String objectKey, String mediaUrl, Long fileSize, String contentType) {

        public static StoredMedia empty() {
            return new StoredMedia(null, null, null, null);
        }
    }
}
