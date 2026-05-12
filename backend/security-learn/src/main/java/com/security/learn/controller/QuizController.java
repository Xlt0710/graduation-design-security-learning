package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.QuizDetailResponse;
import com.security.learn.dto.QuizRecordResponse;
import com.security.learn.dto.QuizSubmitRequest;
import com.security.learn.dto.QuizSubmitResponse;
import com.security.learn.service.QuizService;
import com.security.learn.service.UserService;
import org.springframework.security.core.Authentication;
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
    private final UserService userService;

    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<QuizDetailResponse> detail(@PathVariable Long id) {
        return Result.success(quizService.getQuizDetail(id));
    }

    @PostMapping("/{id}/submit")
    public Result<QuizSubmitResponse> submit(
            @PathVariable Long id,
            @RequestBody QuizSubmitRequest request,
            Authentication authentication) {
        Long userId = requireUserId(authentication);
        return Result.success(quizService.submitQuiz(id, request.getAnswers(), userId));
    }

    @GetMapping("/records")
    public Result<List<QuizRecordResponse>> records(Authentication authentication) {
        Long userId = requireUserId(authentication);
        return Result.success(quizService.getRecords(userId));
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("请先登录");
        }
        return userService.getUserIdByUsername(authentication.getName());
    }
}
