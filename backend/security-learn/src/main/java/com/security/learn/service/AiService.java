package com.security.learn.service;

import com.security.learn.dto.ChatRequest;
import com.security.learn.dto.ChatResponse;
import com.security.learn.dto.ConversationHistoryResponse;
import com.security.learn.dto.RecommendationResponse;

import java.util.List;

public interface AiService {

    RecommendationResponse getRecommendations(Long userId);

    RecommendationResponse refreshRecommendations(Long userId);

    ChatResponse chat(Long userId, ChatRequest request);

    List<ConversationHistoryResponse> getConversations(Long userId);
}
