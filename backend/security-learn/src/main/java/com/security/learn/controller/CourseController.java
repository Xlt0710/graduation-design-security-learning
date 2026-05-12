package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.CourseDetailResponse;
import com.security.learn.dto.CourseListResponse;
import com.security.learn.service.CourseService;
import com.security.learn.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;

    public CourseController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    @GetMapping
    public Result<List<CourseListResponse>> list(
            @RequestParam(required = false) Integer difficulty,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return Result.success(courseService.getCourseList(difficulty, userId));
    }

    @GetMapping("/{id}")
    public Result<CourseDetailResponse> detail(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return Result.success(courseService.getCourseDetail(id, userId));
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userService.getUserIdByUsername(authentication.getName());
    }
}
