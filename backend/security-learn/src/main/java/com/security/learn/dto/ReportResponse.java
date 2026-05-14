package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportResponse {

    private Long id;
    private Long labId;
    private String labTitle;
    private String title;
    private String vulnerabilityType;
    private String riskLevel;
    private String description;
    private String reproduceSteps;
    private String impact;
    private String repairSuggestion;
    private Integer aiGenerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
