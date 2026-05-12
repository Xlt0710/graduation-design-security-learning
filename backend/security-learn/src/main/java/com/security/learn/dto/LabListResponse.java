package com.security.learn.dto;

import lombok.Data;

@Data
public class LabListResponse {

    private Long id;

    private String title;

    private String vulnerabilityType;

    private Integer difficulty;

    private Boolean solved;

    private Integer attemptCount;

    private Boolean favorited;
}
