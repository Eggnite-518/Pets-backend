package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sitter_training_records")
public class SitterTrainingRecordDO extends BaseDO {

    @TableId("provider_id")
    private Long providerId;
    @TableField("learning_completed_at")
    private LocalDateTime learningCompletedAt;
    @TableField("learning_started_at")
    private LocalDateTime learningStartedAt;
    @TableField("learning_progress_json")
    private String learningProgressJson;
    @TableField("last_exam_score")
    private Integer lastExamScore;
    @TableField("last_exam_passed")
    private Integer lastExamPassed;
    @TableField("last_exam_at")
    private LocalDateTime lastExamAt;
    @TableField("exam_question_ids_json")
    private String examQuestionIdsJson;
    @TableField("exam_started_at")
    private LocalDateTime examStartedAt;
    @TableField("reset_reason")
    private String resetReason;
}
