package com.example.pets_backend.service.official;

import com.example.pets_backend.dto.resp.OfficialMessageInboxItemRespDTO;
import com.example.pets_backend.dto.resp.OfficialMessageRespDTO;
import com.example.pets_backend.dto.resp.OfficialMessageSendRespDTO;
import java.util.List;

public interface OfficialMessageService {

    OfficialMessageSendRespDTO sendSystemOfficialMessage(Long orderId, Long receiverId, String content);

    List<OfficialMessageRespDTO> listOfficialMessages(Long orderId, Long currentUserId);

    List<OfficialMessageInboxItemRespDTO> listInbox(Long currentUserId);
}
