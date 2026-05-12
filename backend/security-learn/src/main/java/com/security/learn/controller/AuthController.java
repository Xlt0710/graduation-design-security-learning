package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.LoginRequest;
import com.security.learn.dto.LoginResponse;
import com.security.learn.dto.RegisterRequest;
import com.security.learn.dto.UserInfoResponse;
import com.security.learn.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me(Authentication authentication) {
        return Result.success(userService.getCurrentUser(authentication.getName()));
    }
}
