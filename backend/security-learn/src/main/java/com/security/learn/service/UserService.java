package com.security.learn.service;

import com.security.learn.dto.LoginRequest;
import com.security.learn.dto.LoginResponse;
import com.security.learn.dto.RegisterRequest;
import com.security.learn.dto.ChangePasswordRequest;
import com.security.learn.dto.UpdateProfileRequest;
import com.security.learn.dto.UserInfoResponse;
import com.security.learn.dto.UserStatisticsResponse;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserInfoResponse getCurrentUser(Long userId);

    UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserStatisticsResponse getStatistics(Long userId);
}
