package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.ChangePasswordRequest;
import com.security.learn.dto.UpdateProfileRequest;
import com.security.learn.dto.UserInfoResponse;
import com.security.learn.dto.UserStatisticsResponse;
import com.security.learn.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<UserInfoResponse> profile(Authentication authentication) {
        return Result.success(userService.getCurrentUser(authentication.getName()));
    }

    @PutMapping("/profile")
    public Result<UserInfoResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        Long userId = requireUserId(authentication);
        return Result.success(userService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        Long userId = requireUserId(authentication);
        userService.changePassword(userId, request);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<UserStatisticsResponse> statistics(Authentication authentication) {
        Long userId = requireUserId(authentication);
        return Result.success(userService.getStatistics(userId));
    }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("请先登录");
        }
        return userService.getUserIdByUsername(authentication.getName());
    }
}
