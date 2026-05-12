package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.dto.ChangePasswordRequest;
import com.security.learn.dto.LoginRequest;
import com.security.learn.dto.LoginResponse;
import com.security.learn.dto.RegisterRequest;
import com.security.learn.dto.UpdateProfileRequest;
import com.security.learn.dto.UserInfoResponse;
import com.security.learn.dto.UserStatisticsResponse;
import com.security.learn.entity.LabAttempt;
import com.security.learn.entity.QuizRecord;
import com.security.learn.entity.Role;
import com.security.learn.entity.User;
import com.security.learn.entity.UserChapterProgress;
import com.security.learn.entity.UserCourseProgress;
import com.security.learn.entity.UserRole;
import com.security.learn.mapper.LabAttemptMapper;
import com.security.learn.mapper.QuizRecordMapper;
import com.security.learn.mapper.RoleMapper;
import com.security.learn.mapper.UserChapterProgressMapper;
import com.security.learn.mapper.UserCourseProgressMapper;
import com.security.learn.mapper.UserMapper;
import com.security.learn.mapper.UserRoleMapper;
import com.security.learn.security.JwtUtil;
import com.security.learn.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserCourseProgressMapper userCourseProgressMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final LabAttemptMapper labAttemptMapper;
    private final QuizRecordMapper quizRecordMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper,
                           UserCourseProgressMapper userCourseProgressMapper,
                           UserChapterProgressMapper userChapterProgressMapper,
                           LabAttemptMapper labAttemptMapper, QuizRecordMapper quizRecordMapper,
                           PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userCourseProgressMapper = userCourseProgressMapper;
        this.userChapterProgressMapper = userChapterProgressMapper;
        this.labAttemptMapper = labAttemptMapper;
        this.quizRecordMapper = quizRecordMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        Role userRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, "USER"));
        if (userRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(userRole.getId());
            userRoleMapper.insert(ur);
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("user is disabled");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public Long getUserIdByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getId)
                .eq(User::getUsername, username));
        return user != null ? user.getId() : null;
    }

    @Override
    public UserInfoResponse getCurrentUser(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname(), user.getEmail());
    }

    @Override
    public UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname(), user.getEmail());
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!StringUtils.hasText(request.getOldPassword()) || !StringUtils.hasText(request.getNewPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public UserStatisticsResponse getStatistics(Long userId) {
        Long courseCount = userCourseProgressMapper.selectCount(
                new LambdaQueryWrapper<UserCourseProgress>().eq(UserCourseProgress::getUserId, userId));
        Long completedCourseCount = userCourseProgressMapper.selectCount(
                new LambdaQueryWrapper<UserCourseProgress>()
                        .eq(UserCourseProgress::getUserId, userId)
                        .eq(UserCourseProgress::getStatus, 2));
        Long completedChapterCount = userChapterProgressMapper.selectCount(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .eq(UserChapterProgress::getStatus, 1));
        Long labCount = labAttemptMapper.selectCount(
                new LambdaQueryWrapper<LabAttempt>().eq(LabAttempt::getUserId, userId));
        Long correctLabCount = labAttemptMapper.selectCount(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .eq(LabAttempt::getIsCorrect, 1));
        List<QuizRecord> quizRecords = quizRecordMapper.selectList(
                new LambdaQueryWrapper<QuizRecord>().eq(QuizRecord::getUserId, userId));
        long quizCount = quizRecords.size();
        double avgScore = quizRecords.stream().mapToInt(QuizRecord::getScore).average().orElse(0.0);

        UserStatisticsResponse resp = new UserStatisticsResponse();
        resp.setCourseCount(courseCount.intValue());
        resp.setCompletedCourseCount(completedCourseCount.intValue());
        resp.setCompletedChapterCount(completedChapterCount.intValue());
        resp.setLabCount(labCount.intValue());
        resp.setCorrectLabCount(correctLabCount.intValue());
        resp.setQuizCount((int) quizCount);
        resp.setAverageQuizScore(Math.round(avgScore * 100.0) / 100.0);
        return resp;
    }
}
