package com.security.learn.dto;

import lombok.Data;

@Data
public class CourseListResponse {

    private Long id;

    private String title;

    private String description;

    private String coverUrl;

    private Integer difficulty;

    private Integer chapterCount;

    private Double progress;
}
