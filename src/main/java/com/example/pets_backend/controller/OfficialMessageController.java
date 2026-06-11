package com.example.pets_backend.controller;

import com.example.pets_backend.dto.resp.OfficialMessageInboxItemRespDTO;
import com.example.pets_backend.dto.resp.OfficialMessageRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.official.OfficialMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class OfficialMessageController {

    private final OfficialMessageService officialMessageService;

    @GetMapping("/official")
    public Result<List<OfficialMessageRespDTO>> listOfficialMessages(@RequestParam Long orderId) {
        return Results.success(officialMessageService.listOfficialMessages(orderId, currentUserId()));
    }

    @GetMapping("/official/inbox")
    public Result<List<OfficialMessageInboxItemRespDTO>> listOfficialInbox() {
        return Results.success(officialMessageService.listInbox(currentUserId()));
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}

