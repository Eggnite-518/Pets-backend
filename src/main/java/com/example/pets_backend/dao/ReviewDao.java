package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pets_backend.dao.entity.ReviewDO;
import com.example.pets_backend.dao.mapper.ReviewMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewDao {

    private final ReviewMapper reviewMapper;

    public void insert(ReviewDO review) {
        reviewMapper.insert(review);
    }

    public ReviewDO selectById(Long reviewId) {
        if (reviewId == null) {
            return null;
        }
        return reviewMapper.selectById(reviewId);
    }

    public void updateById(ReviewDO review) {
        reviewMapper.updateById(review);
    }

    public List<ReviewDO> selectByTargetIdAndType(Long targetId, Integer reviewType) {
        LambdaQueryWrapper<ReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDO::getTargetId, targetId)
                .eq(ReviewDO::getReviewType, reviewType)
                .orderByAsc(ReviewDO::getReviewId);
        return reviewMapper.selectList(wrapper);
    }

    public boolean existsByOrderIdAndReviewerAndType(Long orderId, Long reviewerId, Integer reviewType) {
        LambdaQueryWrapper<ReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDO::getOrderId, orderId)
                .eq(ReviewDO::getReviewerId, reviewerId)
                .eq(ReviewDO::getReviewType, reviewType)
                .last("LIMIT 1");
        return reviewMapper.selectOne(wrapper) != null;
    }

    public ReviewDO selectByOrderIdAndType(Long orderId, Integer reviewType) {
        if (orderId == null || reviewType == null) {
            return null;
        }
        LambdaQueryWrapper<ReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDO::getOrderId, orderId)
                .eq(ReviewDO::getReviewType, reviewType)
                .orderByDesc(ReviewDO::getReviewId)
                .last("LIMIT 1");
        return reviewMapper.selectOne(wrapper);
    }

    public IPage<ReviewDO> selectPageByTargetIdAndType(Long targetId, Integer reviewType,
            Integer reviewStatus, Boolean lowScoreOnly, Integer page, Integer pageSize) {
        LambdaQueryWrapper<ReviewDO> wrapper = buildTargetWrapper(targetId, reviewType);
        if (reviewStatus != null) {
            wrapper.eq(ReviewDO::getReviewStatus, reviewStatus);
        }
        if (Boolean.TRUE.equals(lowScoreOnly)) {
            wrapper.eq(ReviewDO::getIsLowScore, 1);
        }
        wrapper.orderByDesc(ReviewDO::getCreatedAt)
                .orderByDesc(ReviewDO::getReviewId);
        return reviewMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public long countLowScoreByTargetIdAndType(Long targetId, Integer reviewType) {
        LambdaQueryWrapper<ReviewDO> wrapper = buildTargetWrapper(targetId, reviewType);
        wrapper.eq(ReviewDO::getIsLowScore, 1);
        Long count = reviewMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    public long countByTargetIdAndTypeAfter(Long targetId, Integer reviewType, LocalDateTime createdAfter,
            Boolean lowScoreOnly) {
        LambdaQueryWrapper<ReviewDO> wrapper = buildTargetWrapper(targetId, reviewType);
        if (createdAfter != null) {
            wrapper.ge(ReviewDO::getCreatedAt, createdAfter);
        }
        if (Boolean.TRUE.equals(lowScoreOnly)) {
            wrapper.eq(ReviewDO::getIsLowScore, 1);
        }
        Long count = reviewMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    public long countByTargetIdAndType(Long targetId, Integer reviewType) {
        if (targetId == null || reviewType == null) {
            return 0L;
        }
        LambdaQueryWrapper<ReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDO::getTargetId, targetId)
                .eq(ReviewDO::getReviewType, reviewType);
        Long count = reviewMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    public long countByTargetIdAndTypeAndScore(Long targetId, Integer reviewType, Integer score) {
        if (targetId == null || reviewType == null || score == null) {
            return 0L;
        }
        LambdaQueryWrapper<ReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDO::getTargetId, targetId)
                .eq(ReviewDO::getReviewType, reviewType)
                .eq(ReviewDO::getScore, score);
        Long count = reviewMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    public boolean existsByOrderIdAndReviewerIdAndReviewType(Long orderId, Long reviewerId, Integer reviewType) {
        return existsByOrderIdAndReviewerAndType(orderId, reviewerId, reviewType);
    }

    private LambdaQueryWrapper<ReviewDO> buildTargetWrapper(Long targetId, Integer reviewType) {
        LambdaQueryWrapper<ReviewDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDO::getTargetId, targetId)
                .eq(ReviewDO::getReviewType, reviewType);
        return wrapper;
    }
}
