package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportListResponse {

    private Long id;
    private String title;
    private String vulnerabilityType;
    private String riskLevel;
    private Integer aiGenerated;
    private LocalDateTime createdAt;
}
