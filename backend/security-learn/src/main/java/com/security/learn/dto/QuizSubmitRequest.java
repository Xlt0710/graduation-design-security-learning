package com.security.learn.dto;

import lombok.Data;

import java.util.Map;

@Data
public class QuizSubmitRequest {

    private Map<Long, String> answers;
}
