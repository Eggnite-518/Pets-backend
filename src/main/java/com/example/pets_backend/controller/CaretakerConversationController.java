package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.ConversationMessageSendReqDTO;
import com.example.pets_backend.dto.resp.ConversationMessageRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.CaretakerConversationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caretaker/conversations")
@RequiredArgsConstructor
public class CaretakerConversationController {

    private final CaretakerConversationService caretakerConversationService;

    @PostMapping("/{conversationId}/messages")
    public Result<ConversationMessageRespDTO> sendMessage(@PathVariable Long conversationId,
            @RequestBody ConversationMessageSendReqDTO reqDTO) {
        return Results.success(caretakerConversationService.sendMessage(conversationId, reqDTO));
    }

    @GetMapping("/{conversationId}/messages")
    public Result<List<ConversationMessageRespDTO>> listMessages(@PathVariable Long conversationId) {
        return Results.success(caretakerConversationService.listMessages(conversationId));
    }
}

