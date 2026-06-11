package com.example.pets_backend.service;

import com.example.pets_backend.dto.resp.ReviewAttachmentUploadRespDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ReviewAttachmentService {

    ReviewAttachmentUploadRespDTO uploadAttachment(MultipartFile file);
}
