package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.dto.CourseDetailResponse;
import com.security.learn.dto.CourseListResponse;
import com.security.learn.entity.Chapter;
import com.security.learn.entity.Course;
import com.security.learn.entity.UserChapterProgress;
import com.security.learn.entity.UserCourseProgress;
import com.security.learn.mapper.ChapterMapper;
import com.security.learn.mapper.CourseMapper;
import com.security.learn.mapper.UserChapterProgressMapper;
import com.security.learn.mapper.UserCourseProgressMapper;
import com.security.learn.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final ChapterMapper chapterMapper;
    private final UserCourseProgressMapper userCourseProgressMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;

    public CourseServiceImpl(CourseMapper courseMapper, ChapterMapper chapterMapper,
                             UserCourseProgressMapper userCourseProgressMapper,
                             UserChapterProgressMapper userChapterProgressMapper) {
        this.courseMapper = courseMapper;
        this.chapterMapper = chapterMapper;
        this.userCourseProgressMapper = userCourseProgressMapper;
        this.userChapterProgressMapper = userChapterProgressMapper;
    }

    @Override
    public List<CourseListResponse> getCourseList(Integer difficulty, Long userId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getSortOrder);
        if (difficulty != null) {
            wrapper.eq(Course::getDifficulty, difficulty);
        }
        List<Course> courses = courseMapper.selectList(wrapper);

        Map<Long, UserCourseProgress> progressMap = Map.of();
        if (userId != null) {
            List<UserCourseProgress> progressList = userCourseProgressMapper.selectList(
                    new LambdaQueryWrapper<UserCourseProgress>().eq(UserCourseProgress::getUserId, userId));
            progressMap = progressList.stream()
                    .collect(Collectors.toMap(UserCourseProgress::getCourseId, p -> p, (a, b) -> a));
        }

        List<CourseListResponse> result = new ArrayList<>();
        for (Course course : courses) {
            CourseListResponse item = new CourseListResponse();
            item.setId(course.getId());
            item.setTitle(course.getTitle());
            item.setDescription(course.getDescription());
            item.setCoverUrl(course.getCoverUrl());
            item.setDifficulty(course.getDifficulty());

            Long chapterCount = chapterMapper.selectCount(
                    new LambdaQueryWrapper<Chapter>().eq(Chapter::getCourseId, course.getId()));
            item.setChapterCount(chapterCount.intValue());

            UserCourseProgress progress = progressMap.get(course.getId());
            item.setProgress(progress != null ? progress.getProgress().doubleValue() : 0.0);

            result.add(item);
        }
        return result;
    }

    @Override
    public CourseDetailResponse getCourseDetail(Long courseId, Long userId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }

        CourseDetailResponse resp = new CourseDetailResponse();
        resp.setId(course.getId());
        resp.setTitle(course.getTitle());
        resp.setDescription(course.getDescription());
        resp.setCoverUrl(course.getCoverUrl());
        resp.setDifficulty(course.getDifficulty());

        List<Chapter> chapters = chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getCourseId, courseId)
                        .orderByAsc(Chapter::getSortOrder));

        Map<Long, Boolean> completedMap = Map.of();
        if (userId != null) {
            List<UserChapterProgress> completed = userChapterProgressMapper.selectList(
                    new LambdaQueryWrapper<UserChapterProgress>()
                            .eq(UserChapterProgress::getUserId, userId)
                            .eq(UserChapterProgress::getStatus, 1));
            completedMap = completed.stream()
                    .collect(Collectors.toMap(UserChapterProgress::getChapterId, p -> true, (a, b) -> a));
        }

        List<CourseDetailResponse.ChapterItem> chapterItems = new ArrayList<>();
        for (Chapter ch : chapters) {
            CourseDetailResponse.ChapterItem item = new CourseDetailResponse.ChapterItem();
            item.setId(ch.getId());
            item.setTitle(ch.getTitle());
            item.setSortOrder(ch.getSortOrder());
            item.setCompleted(completedMap.getOrDefault(ch.getId(), false));
            chapterItems.add(item);
        }
        resp.setChapters(chapterItems);

        UserCourseProgress progress = null;
        if (userId != null) {
            progress = userCourseProgressMapper.selectOne(
                    new LambdaQueryWrapper<UserCourseProgress>()
                            .eq(UserCourseProgress::getUserId, userId)
                            .eq(UserCourseProgress::getCourseId, courseId));
        }
        resp.setProgress(progress != null ? progress.getProgress().doubleValue() : 0.0);

        return resp;
    }
}
