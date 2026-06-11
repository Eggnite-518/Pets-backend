package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.mapper.SitterTrainingRecordMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SitterTrainingRecordDao {

    private final SitterTrainingRecordMapper mapper;

    public SitterTrainingRecordDO selectById(Long providerId) {
        return mapper.selectById(providerId);
    }

    public void insert(SitterTrainingRecordDO record) {
        mapper.insert(record);
    }

    public void updateById(SitterTrainingRecordDO record) {
        mapper.updateById(record);
    }

    public void resetProgressForProvider(Long providerId, String reason) {
        LambdaUpdateWrapper<SitterTrainingRecordDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SitterTrainingRecordDO::getProviderId, providerId)
                .set(SitterTrainingRecordDO::getLearningCompletedAt, null)
                .set(SitterTrainingRecordDO::getLearningStartedAt, null)
                .set(SitterTrainingRecordDO::getLearningProgressJson, null)
                .set(SitterTrainingRecordDO::getLastExamScore, null)
                .set(SitterTrainingRecordDO::getLastExamPassed, null)
                .set(SitterTrainingRecordDO::getLastExamAt, null)
                .set(SitterTrainingRecordDO::getExamQuestionIdsJson, null)
                .set(SitterTrainingRecordDO::getExamStartedAt, null)
                .set(SitterTrainingRecordDO::getResetReason, reason)
                .set(SitterTrainingRecordDO::getUpdatedAt, LocalDateTime.now());
        mapper.update(null, wrapper);
    }
}

