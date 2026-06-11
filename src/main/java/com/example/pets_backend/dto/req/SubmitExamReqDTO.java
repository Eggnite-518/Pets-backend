package com.example.pets_backend.dto.req;

import java.util.List;

public record SubmitExamReqDTO(List<ExamAnswerDTO> answers) {
}

