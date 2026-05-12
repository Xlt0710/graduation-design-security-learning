package com.security.learn.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizDetailResponse {

    private Long id;

    private Long courseId;

    private Long chapterId;

    private String title;

    private Integer totalScore;

    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem {

        private Long id;

        private Integer questionType;

        private String content;

        private String optionsJson;

        private Integer score;
    }
}
