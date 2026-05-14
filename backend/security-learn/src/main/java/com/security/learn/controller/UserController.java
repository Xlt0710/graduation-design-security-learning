package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.ChangePasswordRequest;
import com.security.learn.dto.UpdateProfileRequest;
import com.security.learn.dto.UserInfoResponse;
import com.security.learn.dto.UserStatisticsResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.UserService;
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
    public Result<UserInfoResponse> profile() {
        return Result.success(userService.getCurrentUser(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/profile")
    public Result<UserInfoResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return Result.success(userService.updateProfile(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(SecurityUtils.getCurrentUserId(), request);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<UserStatisticsResponse> statistics() {
        return Result.success(userService.getStatistics(SecurityUtils.getCurrentUserId()));
    }
}
