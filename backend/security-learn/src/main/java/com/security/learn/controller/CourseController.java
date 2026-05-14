package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.CourseDetailResponse;
import com.security.learn.dto.CourseListResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.CourseService;
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

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public Result<List<CourseListResponse>> list(
            @RequestParam(required = false) Integer difficulty) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(courseService.getCourseList(difficulty, userId));
    }

    @GetMapping("/{id}")
    public Result<CourseDetailResponse> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(courseService.getCourseDetail(id, userId));
    }
}
