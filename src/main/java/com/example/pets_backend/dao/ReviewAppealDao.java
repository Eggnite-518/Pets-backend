package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pets_backend.dao.entity.ReviewAppealDO;
import com.example.pets_backend.dao.mapper.ReviewAppealMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewAppealDao {

    private final ReviewAppealMapper reviewAppealMapper;

    public void insert(ReviewAppealDO appeal) {
        reviewAppealMapper.insert(appeal);
    }

    public ReviewAppealDO selectById(Long appealId) {
        if (appealId == null) {
            return null;
        }
        return reviewAppealMapper.selectById(appealId);
    }

    public void updateById(ReviewAppealDO appeal) {
        reviewAppealMapper.updateById(appeal);
    }

    public ReviewAppealDO selectLatestByReviewId(Long reviewId) {
        if (reviewId == null) {
            return null;
        }
        LambdaQueryWrapper<ReviewAppealDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewAppealDO::getReviewId, reviewId)
                .orderByDesc(ReviewAppealDO::getCreatedAt)
                .orderByDesc(ReviewAppealDO::getAppealId)
                .last("LIMIT 1");
        return reviewAppealMapper.selectOne(wrapper);
    }

    public IPage<ReviewAppealDO> selectPage(Integer page, Integer pageSize, Integer appealStatus) {
        LambdaQueryWrapper<ReviewAppealDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(appealStatus != null, ReviewAppealDO::getAppealStatus, appealStatus)
                .orderByDesc(ReviewAppealDO::getCreatedAt)
                .orderByDesc(ReviewAppealDO::getAppealId);
        return reviewAppealMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public boolean existsActiveByReviewId(Long reviewId) {
        if (reviewId == null) {
            return false;
        }
        LambdaQueryWrapper<ReviewAppealDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewAppealDO::getReviewId, reviewId)
                .in(ReviewAppealDO::getAppealStatus, List.of(1, 2))
                .last("LIMIT 1");
        return reviewAppealMapper.selectOne(wrapper) != null;
    }
}
