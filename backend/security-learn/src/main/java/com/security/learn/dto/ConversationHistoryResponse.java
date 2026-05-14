package com.security.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationHistoryResponse {

    private Long id;
    private String scene;
    private String prompt;
    private String response;
    private LocalDateTime createdAt;
}
