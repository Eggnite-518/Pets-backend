package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.ReviewDeductionReasonDO;
import com.example.pets_backend.dao.mapper.ReviewDeductionReasonMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewDeductionReasonDao {

    private final ReviewDeductionReasonMapper reviewDeductionReasonMapper;

    public void insertBatch(List<ReviewDeductionReasonDO> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return;
        }
        reasons.forEach(reviewDeductionReasonMapper::insert);
    }

    public List<ReviewDeductionReasonDO> selectByReviewIds(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ReviewDeductionReasonDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ReviewDeductionReasonDO::getReviewId, reviewIds)
                .orderByAsc(ReviewDeductionReasonDO::getReasonType)
                .orderByAsc(ReviewDeductionReasonDO::getReasonId);
        return reviewDeductionReasonMapper.selectList(wrapper);
    }

    public boolean existsByReviewId(Long reviewId) {
        if (reviewId == null) {
            return false;
        }
        LambdaQueryWrapper<ReviewDeductionReasonDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewDeductionReasonDO::getReviewId, reviewId)
                .last("LIMIT 1");
        return reviewDeductionReasonMapper.selectOne(wrapper) != null;
    }
}
