package com.security.learn.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizSubmitResponse {

    private Integer score;

    private Integer totalScore;

    private List<QuestionResult> results;

    @Data
    public static class QuestionResult {

        private Long questionId;

        private Boolean correct;

        private String userAnswer;

        private String correctAnswer;

        private String analysis;

        private Integer score;
    }
}
