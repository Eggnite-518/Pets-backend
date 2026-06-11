package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.ReviewAttachmentDO;
import com.example.pets_backend.dao.mapper.ReviewAttachmentMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewAttachmentDao {

    private final ReviewAttachmentMapper reviewAttachmentMapper;

    public void insertBatch(List<ReviewAttachmentDO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        attachments.forEach(reviewAttachmentMapper::insert);
    }

    public List<ReviewAttachmentDO> selectByReviewIds(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ReviewAttachmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ReviewAttachmentDO::getReviewId, reviewIds)
                .orderByAsc(ReviewAttachmentDO::getSortOrder)
                .orderByAsc(ReviewAttachmentDO::getAttachmentId);
        return reviewAttachmentMapper.selectList(wrapper);
    }
}
