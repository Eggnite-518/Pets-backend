package com.example.pets_backend.controller;

import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @PostMapping("/deactivate")
    public Result<Void> deactivate() {
        userService.deactivateAccount(UserContext.getUserId());
        return Results.success();
    }

    @PostMapping("/apply-caretaker")
    public Result<Map<String, String>> applyCaretaker() {
        String newToken = userService.applyCaretaker(UserContext.getUserId());
        return Results.success(Map.of("token", newToken));
    }

    /**
     * 实名认证
     *
     * <p>Content-Type: multipart/form-data
     * <ul>
     *   <li>realName    – 真实姓名（必填，≥ 2 字）</li>
     *   <li>idCardNo    – 身份证号码（必填，18 位，末位可为 X）</li>
     *   <li>frontImage  – 身份证人像面原图（选填，二进制文件）</li>
     *   <li>backImage   – 身份证国徽面原图（选填，二进制文件）</li>
     * </ul>
     * 图片选填，提供后上传至 OSS 留存；realName 与 idCardNo 写入 users 表。
     */
    @PostMapping(value = "/real-name-verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Void> realNameVerify(
            @RequestParam String realName,
            @RequestParam String idCardNo,
            @RequestParam(required = false) MultipartFile frontImage,
            @RequestParam(required = false) MultipartFile backImage) {
        userService.realNameVerify(UserContext.getUserId(), realName, idCardNo, frontImage, backImage);
        return Results.success();
    }
}
