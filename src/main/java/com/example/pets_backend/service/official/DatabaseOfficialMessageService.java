package com.example.pets_backend.service.official;

import com.example.pets_backend.config.OfficialMessageProperties;
import com.example.pets_backend.dao.ChatDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.ChatDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.OfficialMessageInboxItemRespDTO;
import com.example.pets_backend.dto.resp.OfficialMessageRespDTO;
import com.example.pets_backend.dto.resp.OfficialMessageSendRespDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseOfficialMessageService implements OfficialMessageService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChatDao chatDao;
    private final OrderDao orderDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final UserDao userDao;
    private final OfficialMessageProperties officialMessageProperties;

    @Override
    @Transactional
    public OfficialMessageSendRespDTO sendSystemOfficialMessage(Long orderId, Long receiverId, String content) {
        Long systemSenderId = officialMessageProperties.getSystemSenderId();
        validateMessage(orderId, systemSenderId, receiverId, content);
        requireOrder(orderId);
        requireUser(receiverId);
        requireUser(systemSenderId);
        return persistMessage(orderId, systemSenderId, receiverId, content);
    }

    @Override
    public List<OfficialMessageRespDTO> listOfficialMessages(Long orderId, Long currentUserId) {
        if (orderId == null || currentUserId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        OrderDO order = requireOrder(orderId);
        ensureCanAccessOfficialMessages(order, currentUserId);
        Long systemSenderId = officialMessageProperties.getSystemSenderId();
        return chatDao.selectOfficialByOrderIdAndReceiverId(orderId, currentUserId, systemSenderId).stream()
                .map(this::toRespDTO)
                .toList();
    }

    @Override
    public List<OfficialMessageInboxItemRespDTO> listInbox(Long currentUserId) {
        if (currentUserId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long systemSenderId = officialMessageProperties.getSystemSenderId();
        List<ChatDO> messages = chatDao.selectOfficialByReceiverId(currentUserId, systemSenderId);
        if (messages.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ChatDO>> grouped = new LinkedHashMap<>();
        for (ChatDO message : messages) {
            if (message.getOrderId() == null) {
                continue;
            }
            grouped.computeIfAbsent(message.getOrderId(), ignored -> new ArrayList<>()).add(message);
        }
        if (grouped.isEmpty()) {
            return List.of();
        }

        Map<Long, String> petNameByOrderId = orderPetSnapshotDao.selectByOrderIds(new ArrayList<>(grouped.keySet())).stream()
                .filter(snapshot -> snapshot.getOrderId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        OrderPetSnapshotDO::getOrderId,
                        snapshot -> snapshot.getSnapPetName() == null ? "" : snapshot.getSnapPetName().trim(),
                        (left, right) -> left.isBlank() ? right : left));

        return grouped.entrySet().stream()
                .map(entry -> toInboxItem(entry.getKey(), entry.getValue(), petNameByOrderId.get(entry.getKey())))
                .sorted(Comparator.comparing(OfficialMessageInboxItemRespDTO::createdAt).reversed())
                .toList();
    }

    private OfficialMessageSendRespDTO persistMessage(Long orderId, Long senderId, Long receiverId, String content) {
        ChatDO chat = new ChatDO();
        LocalDateTime now = LocalDateTime.now();
        chat.setOrderId(orderId);
        chat.setSenderId(senderId);
        chat.setReceiverId(receiverId);
        chat.setContent(content.trim());
        chat.setIsRead(false);
        chat.setCreatedAt(now);
        chat.setUpdatedAt(now);
        chat.setDeleted(0);
        chatDao.insert(chat);
        log.info("Official message stored, orderId={}, senderId={}, receiverId={}, messageId={}",
                orderId, senderId, receiverId, chat.getMessageId());
        return new OfficialMessageSendRespDTO(chat.getMessageId(), orderId, senderId, receiverId, chat.getContent(),
                DATE_TIME_FORMATTER.format(now));
    }

    private OfficialMessageRespDTO toRespDTO(ChatDO chat) {
        return new OfficialMessageRespDTO(
                chat.getMessageId(),
                chat.getOrderId(),
                chat.getSenderId(),
                chat.getReceiverId(),
                chat.getContent(),
                chat.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(chat.getCreatedAt()));
    }

    private void validateMessage(Long orderId, Long senderId, Long receiverId, String content) {
        if (orderId == null || senderId == null || receiverId == null || content == null || content.isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private void ensureCanAccessOfficialMessages(OrderDO order, Long currentUserId) {
        if (currentUserId.equals(order.getOwnerId()) || currentUserId.equals(order.getProviderId())) {
            return;
        }
        Long systemSenderId = officialMessageProperties.getSystemSenderId();
        boolean hasOfficialMessages = !chatDao
                .selectOfficialByOrderIdAndReceiverId(order.getOrderId(), currentUserId, systemSenderId)
                .isEmpty();
        if (!hasOfficialMessages) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
    }

    private OfficialMessageInboxItemRespDTO toInboxItem(Long orderId, List<ChatDO> messages, String petName) {
        ChatDO latest = messages.stream()
                .max(Comparator.comparing(ChatDO::getMessageId, Comparator.nullsLast(Long::compareTo)))
                .orElse(messages.get(0));
        String title = petName == null || petName.isBlank()
                ? "订单 #" + orderId
                : petName + " 的服务通知";
        String createdAt = latest.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(latest.getCreatedAt());
        return new OfficialMessageInboxItemRespDTO(
                orderId,
                title,
                "订单 #" + orderId,
                latest.getContent(),
                createdAt,
                messages.size());
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    private UserDO requireUser(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return user;
    }
}
