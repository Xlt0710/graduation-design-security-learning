package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.ChatRequest;
import com.security.learn.dto.ChatResponse;
import com.security.learn.dto.ConversationHistoryResponse;
import com.security.learn.dto.RecommendationResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/recommendations")
    public Result<RecommendationResponse> getRecommendations() {
        Long userId = requireUserId();
        return Result.success(aiService.getRecommendations(userId));
    }

    @PostMapping("/recommendations/refresh")
    public Result<RecommendationResponse> refreshRecommendations() {
        Long userId = requireUserId();
        return Result.success(aiService.refreshRecommendations(userId));
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        Long userId = requireUserId();
        return Result.success(aiService.chat(userId, request));
    }

    @GetMapping("/conversations")
    public Result<List<ConversationHistoryResponse>> getConversations() {
        Long userId = requireUserId();
        return Result.success(aiService.getConversations(userId));
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
