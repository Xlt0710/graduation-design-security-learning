package com.security.learn.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseDetailResponse {

    private Long id;

    private String title;

    private String description;

    private String coverUrl;

    private Integer difficulty;

    private Double progress;

    private List<ChapterItem> chapters;

    @Data
    public static class ChapterItem {

        private Long id;

        private String title;

        private Integer sortOrder;

        private Boolean completed;
    }
}
