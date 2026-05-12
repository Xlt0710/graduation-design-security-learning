package com.security.learn.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LabAttemptResponse {

    private Long id;

    private Long labId;

    private String labTitle;

    private Boolean isCorrect;

    private Boolean usedHint;

    private Integer attemptCount;

    private LocalDateTime submittedAt;
}
