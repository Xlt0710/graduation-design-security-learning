package com.security.learn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizRecordResponse {

    private Long id;

    private Long quizId;

    private String quizTitle;

    private Integer score;

    private LocalDateTime submittedAt;
}
