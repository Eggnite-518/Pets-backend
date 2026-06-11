package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.pets_backend.config.VideoUploadProperties;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class VideoUploadServiceTest {

    private VideoUploadService videoUploadService;

    @BeforeEach
    void setUp() {
        videoUploadService = new VideoUploadService(new VideoUploadProperties());
    }

    @Test
    void validateVideoRejectsMissingFileWithDedicatedCode() {
        ClientException exception = assertThrows(ClientException.class,
                () -> videoUploadService.validateVideo(null, 30));

        assertEquals(BaseErrorCode.VIDEO_FILE_REQUIRED_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void validateVideoRejectsUnsupportedContentTypeWithDedicatedCode() {
        MockMultipartFile file = new MockMultipartFile("file", "proof.mp4", "text/plain", new byte[] {1});

        ClientException exception = assertThrows(ClientException.class,
                () -> videoUploadService.validateVideo(file, 30));

        assertEquals(BaseErrorCode.VIDEO_CONTENT_TYPE_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void validateVideoRejectsUnsupportedExtensionWithDedicatedCode() {
        MockMultipartFile file = new MockMultipartFile("file", "proof.txt", "video/mp4", new byte[] {1});

        ClientException exception = assertThrows(ClientException.class,
                () -> videoUploadService.validateVideo(file, 30));

        assertEquals(BaseErrorCode.VIDEO_EXTENSION_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void validateVideoRejectsTooHighFrameRateWithDedicatedCode() {
        MockMultipartFile file = new MockMultipartFile("file", "proof.mp4", "video/mp4", new byte[] {1});

        ClientException exception = assertThrows(ClientException.class,
                () -> videoUploadService.validateVideo(file, 120));

        assertEquals(BaseErrorCode.VIDEO_FRAME_RATE_ERROR.code(), exception.getErrorCode());
    }
}
