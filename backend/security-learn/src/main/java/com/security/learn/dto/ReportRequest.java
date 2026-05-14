package com.security.learn.dto;

import lombok.Data;

@Data
public class ReportRequest {

    private String title;

    private String vulnerabilityType;

    private String riskLevel;

    private String description;

    private String reproduceSteps;

    private String impact;

    private String repairSuggestion;
}
