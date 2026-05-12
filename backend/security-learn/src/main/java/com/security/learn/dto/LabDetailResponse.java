package com.security.learn.dto;

import lombok.Data;

@Data
public class LabDetailResponse {

    private Long id;

    private String title;

    private String description;

    private String vulnerabilityType;

    private Integer difficulty;

    private String targetUrl;

    private String hint;

    private Boolean hintUsed;

    private Boolean solved;

    private Integer attemptCount;

    private Boolean favorited;
}
