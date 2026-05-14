package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.QuizDetailResponse;
import com.security.learn.dto.QuizRecordResponse;
import com.security.learn.dto.QuizSubmitRequest;
import com.security.learn.dto.QuizSubmitResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.QuizService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{id}")
    public Result<QuizDetailResponse> detail(@PathVariable Long id) {
        return Result.success(quizService.getQuizDetail(id));
    }

    @PostMapping("/{id}/submit")
    public Result<QuizSubmitResponse> submit(
            @PathVariable Long id,
            @RequestBody QuizSubmitRequest request) {
        Long userId = requireUserId();
        return Result.success(quizService.submitQuiz(id, request.getAnswers(), userId));
    }

    @GetMapping("/records")
    public Result<List<QuizRecordResponse>> records() {
        Long userId = requireUserId();
        return Result.success(quizService.getRecords(userId));
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
