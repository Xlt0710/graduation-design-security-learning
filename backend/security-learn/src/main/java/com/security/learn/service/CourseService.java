package com.security.learn.service;

import com.security.learn.dto.CourseDetailResponse;
import com.security.learn.dto.CourseListResponse;

import java.util.List;

public interface CourseService {

    List<CourseListResponse> getCourseList(Integer difficulty, Long userId);

    CourseDetailResponse getCourseDetail(Long courseId, Long userId);
}
