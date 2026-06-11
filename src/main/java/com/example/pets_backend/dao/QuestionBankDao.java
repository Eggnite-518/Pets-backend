package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.QuestionBankDO;
import com.example.pets_backend.dao.mapper.QuestionBankMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuestionBankDao {

    private final QuestionBankMapper mapper;

    public List<QuestionBankDO> selectRandomByType(int type, int limit) {
        return mapper.selectRandomByType(type, limit);
    }

    public List<QuestionBankDO> selectByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.selectBatchIds(ids);
    }

    public void insert(QuestionBankDO question) {
        mapper.insert(question);
    }

    public QuestionBankDO selectById(Long questionId) {
        if (questionId == null) {
            return null;
        }
        return mapper.selectById(questionId);
    }

    public void updateById(QuestionBankDO question) {
        mapper.updateById(question);
    }

    public void deleteById(Long questionId) {
        if (questionId == null) {
            return;
        }
        mapper.deleteById(questionId);
    }

    public List<QuestionBankDO> selectListByFilters(Integer questionType, String keyword) {
        LambdaQueryWrapper<QuestionBankDO> wrapper = new LambdaQueryWrapper<>();
        if (questionType != null) {
            wrapper.eq(QuestionBankDO::getQuestionType, questionType);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(QuestionBankDO::getContent, keyword.trim());
        }
        wrapper.orderByDesc(QuestionBankDO::getQuestionId);
        return mapper.selectList(wrapper);
    }
}
