package com.example.pets_backend.service;

import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.example.pets_backend.config.OcrProperties;
import com.example.pets_backend.dto.resp.IdCardOcrRespDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.infrastructure.oss.OssUploadCommand;
import com.example.pets_backend.infrastructure.oss.OssUploadResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private static final String SIDE_FACE = "face";
    private static final String SIDE_BACK = "back";

    private final OcrProperties ocrProperties;
    private final ObjectStorageService objectStorageService;

    public IdCardOcrRespDTO recognizeIdCard(MultipartFile file, String side) {
        if (file == null || file.isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        String imageSide = (side != null && SIDE_BACK.equalsIgnoreCase(side)) ? SIDE_BACK : SIDE_FACE;

        OssUploadResult ossResult = uploadToOss(file);
        String imageUrl = objectStorageService.generatePresignedUrl(ossResult.objectKey());
        log.info("OCR image URL (presigned): {}", imageUrl);

        try {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                    ocrProperties.getAccessKeyId(), ocrProperties.getAccessKeySecret());
            IAcsClient client = new DefaultAcsClient(profile);

            com.aliyuncs.CommonRequest request = new com.aliyuncs.CommonRequest();
            request.setSysDomain("ocr-api.cn-hangzhou.aliyuncs.com");
            request.setSysVersion("2021-07-07");
            request.setSysAction("RecognizeIdcard");
            request.setMethod(MethodType.POST);
            request.putQueryParameter("Url", imageUrl);
            request.putBodyParameter("Side", imageSide);

            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            log.info("OCR raw response: {}", data);
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            String dataStr = json.get("Data").getAsString();
            JsonObject innerData = JsonParser.parseString(dataStr).getAsJsonObject();
            JsonObject faceData = innerData.getAsJsonObject("data")
                    .getAsJsonObject("face").getAsJsonObject("data");
            if (faceData == null) {
                throw new ClientException(BaseErrorCode.OCR_RECOGNITION_ERROR);
            }

            return new IdCardOcrRespDTO(
                    nullToNullString(faceData, "name"),
                    nullToNullString(faceData, "idNumber"),
                    nullToNullString(faceData, "birthDate"),
                    nullToNullString(faceData, "sex"),
                    nullToNullString(faceData, "ethnicity"),
                    nullToNullString(faceData, "address"));
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR recognition error", e);
            throw new ClientException(BaseErrorCode.OCR_RECOGNITION_ERROR);
        }
    }

    private String nullToNullString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private OssUploadResult uploadToOss(MultipartFile file) {
        String objectKey = "ocr/id-card/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            return objectStorageService.upload(
                    new OssUploadCommand(objectKey, inputStream, file.getSize(), file.getContentType()));
        } catch (IOException ex) {
            throw new ClientException(BaseErrorCode.OSS_UPLOAD_ERROR);
        }
    }
}
