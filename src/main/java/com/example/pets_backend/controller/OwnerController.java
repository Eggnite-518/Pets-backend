package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.ConversationMessageSendReqDTO;
import com.example.pets_backend.dto.resp.CaretakerConversationRespDTO;
import com.example.pets_backend.dto.resp.ConversationMessageRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.OwnerConversationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerConversationService ownerConversationService;

    @GetMapping("/me/conversations")
    public Result<List<CaretakerConversationRespDTO>> listConversations() {
        return Results.success(ownerConversationService.listConversations());
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Result<List<ConversationMessageRespDTO>> listMessages(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long peerId) {
        return Results.success(ownerConversationService.listMessages(conversationId, peerId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public Result<ConversationMessageRespDTO> sendMessage(@PathVariable Long conversationId,
            @RequestBody ConversationMessageSendReqDTO reqDTO) {
        return Results.success(ownerConversationService.sendMessage(conversationId, reqDTO));
    }
}
