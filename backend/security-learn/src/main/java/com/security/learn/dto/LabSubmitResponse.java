package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabSubmitResponse {

    private Boolean correct;

    private String message;

    private Integer attemptCount;
}
