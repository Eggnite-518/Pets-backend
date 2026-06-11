package com.example.pets_backend.dto.resp;

import java.util.List;

public record ExamQuestionDTO(
        Long questionId,
        Integer questionType,
        String content,
        List<String> options) {
}

