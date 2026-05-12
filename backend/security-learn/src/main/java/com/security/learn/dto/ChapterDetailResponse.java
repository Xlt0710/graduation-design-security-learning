package com.security.learn.dto;

import lombok.Data;

@Data
public class ChapterDetailResponse {

    private Long id;

    private Long courseId;

    private String title;

    private String content;

    private Integer sortOrder;

    private Boolean completed;
}
