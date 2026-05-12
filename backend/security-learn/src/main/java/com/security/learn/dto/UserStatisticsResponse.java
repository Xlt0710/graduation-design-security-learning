package com.security.learn.dto;

import lombok.Data;

@Data
public class UserStatisticsResponse {

    private Integer courseCount;

    private Integer completedCourseCount;

    private Integer completedChapterCount;

    private Integer labCount;

    private Integer correctLabCount;

    private Integer quizCount;

    private Double averageQuizScore;
}
