package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.pets_backend.dao.entity.ChatDO;
import com.example.pets_backend.dao.mapper.ChatMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatDao {

    private final ChatMapper chatMapper;

    public void insert(ChatDO chat) {
        chatMapper.insert(chat);
    }

    public List<ChatDO> selectByOrderId(Long orderId) {
        LambdaQueryWrapper<ChatDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatDO::getOrderId, orderId)
                .eq(ChatDO::getDeleted, 0)
                .orderByAsc(ChatDO::getSentAt);
        return chatMapper.selectList(wrapper);
    }

    public List<ChatDO> selectOfficialByOrderIdAndReceiverId(Long orderId, Long receiverId, Long systemSenderId) {
        LambdaQueryWrapper<ChatDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatDO::getOrderId, orderId)
                .eq(ChatDO::getReceiverId, receiverId)
                .eq(ChatDO::getSenderId, systemSenderId)
                .orderByAsc(ChatDO::getSentAt);
        return chatMapper.selectList(wrapper);
    }

    public void markAsReadByMessageIds(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<ChatDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ChatDO::getMessageId, messageIds)
                .set(ChatDO::getIsRead, true)
                .set(ChatDO::getUpdatedAt, LocalDateTime.now());
        chatMapper.update(null, wrapper);
    }

    public List<ChatDO> selectOfficialByOrderId(Long orderId, Long systemSenderId) {
        LambdaQueryWrapper<ChatDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatDO::getOrderId, orderId)
                .eq(ChatDO::getSenderId, systemSenderId)
                .orderByAsc(ChatDO::getMessageId);
        return chatMapper.selectList(wrapper);
    }

    /** 查询当前用户参与过聊天（发送方或接收方）的所有订单 ID（去重） */
    public List<Long> selectDistinctOrderIdsByParticipant(Long userId) {
        if (userId == null) {
            return List.of();
        }
        LambdaQueryWrapper<ChatDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ChatDO::getSenderId, userId)
                          .or()
                          .eq(ChatDO::getReceiverId, userId))
               .select(ChatDO::getOrderId);
        return chatMapper.selectList(wrapper).stream()
                .map(ChatDO::getOrderId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }
}
