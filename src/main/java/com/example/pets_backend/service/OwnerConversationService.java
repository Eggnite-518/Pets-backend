package com.example.pets_backend.service;

import com.example.pets_backend.config.OfficialMessageProperties;
import com.example.pets_backend.dao.ChatDao;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.OrderPetSnapshotDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.ChatDO;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerConversationService {

    private static final int ROLE_OWNER = 1;
    private static final int ROLE_BOTH = 3;
    private static final int SENDER_ROLE_OWNER = 1;
    private static final int SENDER_ROLE_CARETAKER = 2;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_TEXT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatDao chatDao;
    private final OrderDao orderDao;
    private final OrderApplicationDao orderApplicationDao;
    private final UserDao userDao;
    private final OrderPetSnapshotDao orderPetSnapshotDao;
    private final OssAccessibleUrlService ossAccessibleUrlService;
    private final OfficialMessageProperties officialMessageProperties;

    public List<CaretakerConversationRespDTO> listConversations() {
        Long currentUserId = currentUserId();
        requireOwnerRole();
        List<OrderDO> orders = orderDao.selectByOwnerId(currentUserId);
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, String> petNameMap = loadPetNameByOrderId(orders.stream()
                .map(OrderDO::getOrderId)
                .filter(Objects::nonNull)
                .toList());
        List<CaretakerConversationRespDTO> result = new ArrayList<>();
        for (OrderDO order : orders) {
            if (order == null || order.getOrderId() == null || order.getOwnerId() == null) {
                continue;
            }
            Long orderId = order.getOrderId();
            Long ownerId = order.getOwnerId();
            List<ChatDO> chats = chatDao.selectByOrderId(orderId);
            Set<Long> peerIds = collectPeerCaretakerIds(order, ownerId, chats);
            if (peerIds.isEmpty()) {
                continue;
            }
            Map<Long, UserDO> providers = userDao.selectByIds(new ArrayList<>(peerIds)).stream()
                    .collect(Collectors.toMap(UserDO::getUserId, provider -> provider, (a, b) -> a));
            String petName = petNameMap.get(orderId);
            for (Long peerId : peerIds) {
                UserDO provider = providers.get(peerId);
                String peerName = provider == null ? null : provider.getNickname();
                String peerAvatarUrl = provider == null ? null
                        : ossAccessibleUrlService.toDisplayUrl(provider.getAvatarUrl());
                ChatDO lastPrivate = null;
                int unreadCount = 0;
                for (ChatDO chat : chats) {
                    if (!isBetweenParticipants(chat, ownerId, peerId)) {
                        continue;
                    }
                    lastPrivate = chat;
                    if (Objects.equals(chat.getReceiverId(), currentUserId)
                            && !Boolean.TRUE.equals(chat.getIsRead())) {
                        unreadCount++;
                    }
                }
                String lastMessagePreview = lastPrivate == null ? null : lastPrivate.getContent();
                String lastMessageTimeText = lastPrivate == null || lastPrivate.getSentAt() == null
                        ? null
                        : TIME_TEXT_FORMATTER.format(lastPrivate.getSentAt());
                result.add(new CaretakerConversationRespDTO(
                        String.valueOf(orderId),
                        String.valueOf(orderId),
                        String.valueOf(peerId),
                        peerName,
                        peerAvatarUrl,
                        petName,
                        lastMessagePreview,
                        lastMessageTimeText,
                        unreadCount));
            }
        }
        result.sort(Comparator
                .comparing(CaretakerConversationRespDTO::lastMessageTimeText,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(CaretakerConversationRespDTO::conversationId,
                        Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @Transactional
    public ConversationMessageRespDTO sendMessage(Long conversationId, ConversationMessageSendReqDTO reqDTO) {
        if (conversationId == null || reqDTO == null || reqDTO.content() == null || reqDTO.content().isBlank()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long currentUserId = currentUserId();
        requireOwnerRole();
        OrderDO order = requireOrder(conversationId);
        if (!Objects.equals(order.getOwnerId(), currentUserId)) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        Long receiverId = resolvePeerId(order, reqDTO.peerId());
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
                SENDER_ROLE_OWNER,
                chat.getContent(),
                DATE_TIME_FORMATTER.format(now));
    }

    @Transactional
    public List<ConversationMessageRespDTO> listMessages(Long conversationId, Long peerId) {
        if (conversationId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long currentUserId = currentUserId();
        requireOwnerRole();
        OrderDO order = requireOrder(conversationId);
        if (!Objects.equals(order.getOwnerId(), currentUserId)) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        Long ownerId = order.getOwnerId();
        Long resolvedPeerId = resolvePeerId(order, peerId);
        List<ChatDO> chats = chatDao.selectByOrderId(conversationId).stream()
                .filter(chat -> isBetweenParticipants(chat, ownerId, resolvedPeerId))
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
                        resolveSenderRole(chat.getSenderId(), ownerId, resolvedPeerId),
                        chat.getContent(),
                        chat.getSentAt() == null ? null : DATE_TIME_FORMATTER.format(chat.getSentAt())))
                .toList();
    }

    private Set<Long> collectPeerCaretakerIds(OrderDO order, Long ownerId, List<ChatDO> chats) {
        Set<Long> peerIds = new LinkedHashSet<>();
        Long assignedProviderId = order.getProviderId();
        if (assignedProviderId != null) {
            peerIds.add(assignedProviderId);
            return peerIds;
        }
        for (OrderApplicationDO application : orderApplicationDao.selectApplyingByOrderId(order.getOrderId())) {
            if (application.getProviderId() != null) {
                peerIds.add(application.getProviderId());
            }
        }
        for (ChatDO chat : chats) {
            Long peerId = resolveCaretakerPeer(chat, ownerId);
            if (peerId != null) {
                peerIds.add(peerId);
            }
        }
        return peerIds;
    }

    private Long resolvePeerId(OrderDO order, Long peerId) {
        Long assignedProviderId = order.getProviderId();
        if (assignedProviderId != null) {
            return assignedProviderId;
        }
        if (peerId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validatePeerCanChat(order.getOrderId(), order.getOwnerId(), peerId);
        return peerId;
    }

    private void validatePeerCanChat(Long orderId, Long ownerId, Long peerId) {
        if (orderApplicationDao.existsByOrderIdAndProviderId(orderId, peerId)) {
            return;
        }
        boolean hasChatHistory = chatDao.selectByOrderId(orderId).stream()
                .anyMatch(chat -> isBetweenParticipants(chat, ownerId, peerId));
        if (!hasChatHistory) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }

    private Long resolveCaretakerPeer(ChatDO chat, Long ownerId) {
        if (chat == null || ownerId == null) {
            return null;
        }
        Long systemSenderId = officialMessageProperties.getSystemSenderId();
        Long senderId = chat.getSenderId();
        Long receiverId = chat.getReceiverId();
        if (Objects.equals(senderId, ownerId) && receiverId != null && !Objects.equals(receiverId, ownerId)
                && !Objects.equals(receiverId, systemSenderId)) {
            return receiverId;
        }
        if (Objects.equals(receiverId, ownerId) && senderId != null && !Objects.equals(senderId, ownerId)
                && !Objects.equals(senderId, systemSenderId)) {
            return senderId;
        }
        return null;
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

    private void requireOwnerRole() {
        Integer roleType = UserContext.getRoleType();
        if (roleType == null || (roleType != ROLE_OWNER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
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
