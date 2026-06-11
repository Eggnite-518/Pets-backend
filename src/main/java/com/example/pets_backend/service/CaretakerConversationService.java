package com.example.pets_backend.service;

import com.example.pets_backend.dao.ChatDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.ChatDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.OrderPetSnapshotDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.ConversationMessageSendReqDTO;
import com.example.pets_backend.dto.resp.ConversationMessageRespDTO;
import com.example.pets_backend.dto.resp.CaretakerConversationRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaretakerConversationService {

    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int SENDER_ROLE_OWNER = 1;
    private static final int SENDER_ROLE_CARETAKER = 2;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_TEXT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatDao chatDao;
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final OssAccessibleUrlService ossAccessibleUrlService;

    @Transactional
    public ConversationMessageRespDTO sendMessage(Long conversationId, ConversationMessageSendReqDTO reqDTO) {
        if (conversationId == null || reqDTO == null || reqDTO.content() == null || reqDTO.content().isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long currentUserId = currentUserId();
        requireCaretakerRole();
        OrderDO order = requireOrder(conversationId);
        Long receiverId = order.getOwnerId();
        if (receiverId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        ChatDO chat = new ChatDO();
        LocalDateTime now = LocalDateTime.now();
        chat.setOrderId(conversationId);
        chat.setSenderId(currentUserId);
        chat.setReceiverId(receiverId);
        chat.setContent(reqDTO.content().trim());
        chat.setIsRead(false);
        chat.setSentAt(now);
        chat.setCreatedAt(now);
        chat.setUpdatedAt(now);
        chat.setDeleted(0);
        chatDao.insert(chat);
        return new ConversationMessageRespDTO(
                chat.getMessageId(),
                SENDER_ROLE_CARETAKER,
                chat.getContent(),
                DATE_TIME_FORMATTER.format(now));
    }

    @Transactional
    public List<ConversationMessageRespDTO> listMessages(Long conversationId) {
        if (conversationId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long currentUserId = currentUserId();
        requireCaretakerRole();
        OrderDO order = requireOrder(conversationId);
        Long ownerId = order.getOwnerId();
        // 只加载当前宠托师与宠主之间的消息（多位宠托师互相隔离）
        List<ChatDO> chats = chatDao.selectByOrderId(conversationId).stream()
                .filter(chat -> isBetweenParticipants(chat, ownerId, currentUserId))
                .toList();
        List<Long> unreadMessageIds = chats.stream()
                .filter(chat -> Objects.equals(chat.getReceiverId(), currentUserId))
                .filter(chat -> !Boolean.TRUE.equals(chat.getIsRead()))
                .map(ChatDO::getMessageId)
                .toList();
        if (!unreadMessageIds.isEmpty()) {
            chatDao.markAsReadByMessageIds(unreadMessageIds);
        }
        return chats.stream()
                .map(chat -> new ConversationMessageRespDTO(
                        chat.getMessageId(),
                        resolveSenderRole(chat.getSenderId(), ownerId, currentUserId),
                        chat.getContent(),
                        chat.getSentAt() == null ? null : DATE_TIME_FORMATTER.format(chat.getSentAt())))
                .toList();
    }

    private boolean isBetweenParticipants(ChatDO chat, Long ownerId, Long providerId) {
        if (chat == null || ownerId == null || providerId == null) {
            return false;
        }
        Long senderId = chat.getSenderId();
        Long receiverId = chat.getReceiverId();
        return (Objects.equals(senderId, ownerId) && Objects.equals(receiverId, providerId))
                || (Objects.equals(senderId, providerId) && Objects.equals(receiverId, ownerId));
    }

    private Integer resolveSenderRole(Long senderId, Long ownerId, Long providerId) {
        if (senderId == null || ownerId == null || providerId == null) {
            return null;
        }
        if (senderId.equals(ownerId)) {
            return SENDER_ROLE_OWNER;
        }
        if (senderId.equals(providerId)) {
            return SENDER_ROLE_CARETAKER;
        }
        return null;
    }

    private void requireCaretakerRole() {
        Integer roleType = UserContext.getRoleType();
        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR);
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private OrderDO requireOrder(Long orderId) {
        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        return order;
    }

    public List<CaretakerConversationRespDTO> listConversations() {
        Long currentUserId = currentUserId();
        requireCaretakerRole();

        // 1. 已指派的订单（provider_id = currentUserId）
        List<OrderDO> assignedOrders = orderDao.selectByProviderId(currentUserId);
        java.util.Set<Long> assignedOrderIds = assignedOrders.stream()
                .map(OrderDO::getOrderId).filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 2. 主动发过消息但尚未指派的订单（悬赏中聊天）
        List<Long> chatOrderIds = chatDao.selectDistinctOrderIdsByParticipant(currentUserId);
        List<Long> extraIds = chatOrderIds.stream()
                .filter(id -> !assignedOrderIds.contains(id))
                .toList();
        List<OrderDO> extraOrders = orderDao.selectByIds(extraIds);

        // 合并去重，按 orderId 倒序
        List<OrderDO> orders = new java.util.ArrayList<>();
        orders.addAll(assignedOrders);
        orders.addAll(extraOrders);
        orders.sort((a, b) -> {
            if (a.getOrderId() == null) return 1;
            if (b.getOrderId() == null) return -1;
            return Long.compare(b.getOrderId(), a.getOrderId());
        });

        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> ownerIds = orders.stream()
                .map(OrderDO::getOwnerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserDO> owners = userDao.selectByIds(ownerIds).stream()
                .collect(Collectors.toMap(UserDO::getUserId, owner -> owner, (a, b) -> a));
        Map<Long, String> petNameMap = loadPetNameByOrderId(orders.stream()
                .map(OrderDO::getOrderId)
                .filter(Objects::nonNull)
                .toList());
        List<CaretakerConversationRespDTO> result = new ArrayList<>();
        for (OrderDO order : orders) {
            if (order == null || order.getOwnerId() == null || order.getOrderId() == null) {
                continue;
            }
            Long ownerId = order.getOwnerId();
            UserDO owner = owners.get(ownerId);
            String peerName = owner == null ? null : owner.getNickname();
            String peerAvatarUrl = owner == null ? null
                    : ossAccessibleUrlService.toDisplayUrl(owner.getAvatarUrl());
            String petName = petNameMap.get(order.getOrderId());
            List<ChatDO> chats = chatDao.selectByOrderId(order.getOrderId());
            ChatDO lastPrivate = null;
            int unreadCount = 0;
            for (ChatDO chat : chats) {
                if (!isBetweenParticipants(chat, ownerId, currentUserId)) {
                    continue;
                }
                lastPrivate = chat;
                if (Objects.equals(chat.getReceiverId(), currentUserId) && !Boolean.TRUE.equals(chat.getIsRead())) {
                    unreadCount++;
                }
            }
            String lastMessagePreview = lastPrivate == null ? null : lastPrivate.getContent();
            String lastMessageTimeText = lastPrivate == null || lastPrivate.getSentAt() == null
                    ? null
                    : TIME_TEXT_FORMATTER.format(lastPrivate.getSentAt());
            result.add(new CaretakerConversationRespDTO(
                    String.valueOf(order.getOrderId()),
                    String.valueOf(order.getOrderId()),
                    String.valueOf(ownerId),
                    peerName,
                    peerAvatarUrl,
                    petName,
                    lastMessagePreview,
                    lastMessageTimeText,
                    unreadCount));
        }
        return result;
    }

    private Map<Long, String> loadPetNameByOrderId(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        return orderPetSnapshotDao.selectByOrderIds(orderIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(OrderPetSnapshotDO::getOrderId))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(Objects::nonNull)
                                .map(OrderPetSnapshotDO::getSnapPetName)
                                .filter(name -> name != null && !name.isBlank())
                                .findFirst()
                                .orElse(null)));
    }
}

