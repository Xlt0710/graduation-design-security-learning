package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.LoginRequest;
import com.security.learn.dto.LoginResponse;
import com.security.learn.dto.RegisterRequest;
import com.security.learn.dto.UserInfoResponse;
import com.security.learn.security.LoginRateLimiter;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(UserService userService, LoginRateLimiter loginRateLimiter) {
        this.userService = userService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        if (!loginRateLimiter.isAllowed(clientIp)) {
            return Result.fail(429, "登录请求过于频繁，请稍后再试");
        }
        return Result.success(userService.login(request));
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me() {
        return Result.success(userService.getCurrentUser(SecurityUtils.getCurrentUserId()));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
