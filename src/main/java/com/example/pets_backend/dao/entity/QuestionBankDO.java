package com.example.pets_backend.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.pets_backend.frameworks.database.base.BaseDO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("question_bank")
public class QuestionBankDO extends BaseDO {

    @TableId(value = "question_id", type = IdType.ASSIGN_ID)
    private Long questionId;
    private Integer questionType;
    private String content;
    private String optionsJson;
    private String correctAnswer;
}

