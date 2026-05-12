package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.dto.ChapterDetailResponse;
import com.security.learn.entity.Chapter;
import com.security.learn.entity.Course;
import com.security.learn.entity.UserChapterProgress;
import com.security.learn.entity.UserCourseProgress;
import com.security.learn.mapper.ChapterMapper;
import com.security.learn.mapper.CourseMapper;
import com.security.learn.mapper.UserChapterProgressMapper;
import com.security.learn.mapper.UserCourseProgressMapper;
import com.security.learn.service.ChapterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterMapper chapterMapper;
    private final CourseMapper courseMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserCourseProgressMapper userCourseProgressMapper;

    public ChapterServiceImpl(ChapterMapper chapterMapper, CourseMapper courseMapper,
                              UserChapterProgressMapper userChapterProgressMapper,
                              UserCourseProgressMapper userCourseProgressMapper) {
        this.chapterMapper = chapterMapper;
        this.courseMapper = courseMapper;
        this.userChapterProgressMapper = userChapterProgressMapper;
        this.userCourseProgressMapper = userCourseProgressMapper;
    }

    @Override
    public ChapterDetailResponse getChapterDetail(Long chapterId, Long userId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new IllegalArgumentException("章节不存在");
        }

        ChapterDetailResponse resp = new ChapterDetailResponse();
        resp.setId(chapter.getId());
        resp.setCourseId(chapter.getCourseId());
        resp.setTitle(chapter.getTitle());
        resp.setContent(chapter.getContent());
        resp.setSortOrder(chapter.getSortOrder());

        if (userId != null) {
            UserChapterProgress progress = userChapterProgressMapper.selectOne(
                    new LambdaQueryWrapper<UserChapterProgress>()
                            .eq(UserChapterProgress::getUserId, userId)
                            .eq(UserChapterProgress::getChapterId, chapterId));
            resp.setCompleted(progress != null && progress.getStatus() == 1);
        } else {
            resp.setCompleted(false);
        }

        return resp;
    }

    @Override
    @Transactional
    public void completeChapter(Long chapterId, Long userId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new IllegalArgumentException("章节不存在");
        }

        UserChapterProgress progress = userChapterProgressMapper.selectOne(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .eq(UserChapterProgress::getChapterId, chapterId));

        if (progress == null) {
            progress = new UserChapterProgress();
            progress.setUserId(userId);
            progress.setChapterId(chapterId);
            progress.setStatus(1);
            progress.setCompletedAt(LocalDateTime.now());
            userChapterProgressMapper.insert(progress);
        } else if (progress.getStatus() == 0) {
            progress.setStatus(1);
            progress.setCompletedAt(LocalDateTime.now());
            userChapterProgressMapper.updateById(progress);
        }

        Long courseId = chapter.getCourseId();
        Long totalChapters = chapterMapper.selectCount(
                new LambdaQueryWrapper<Chapter>().eq(Chapter::getCourseId, courseId));
        Long completedChapters = userChapterProgressMapper.selectCount(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .eq(UserChapterProgress::getStatus, 1)
                        .inSql(UserChapterProgress::getChapterId,
                                "SELECT id FROM chapter WHERE course_id = " + courseId));

        BigDecimal progressValue = BigDecimal.valueOf(completedChapters)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalChapters), 2, RoundingMode.HALF_UP);

        UserCourseProgress courseProgress = userCourseProgressMapper.selectOne(
                new LambdaQueryWrapper<UserCourseProgress>()
                        .eq(UserCourseProgress::getUserId, userId)
                        .eq(UserCourseProgress::getCourseId, courseId));

        if (courseProgress == null) {
            courseProgress = new UserCourseProgress();
            courseProgress.setUserId(userId);
            courseProgress.setCourseId(courseId);
            courseProgress.setProgress(progressValue);
            courseProgress.setStatus(progressValue.compareTo(BigDecimal.valueOf(100)) >= 0 ? 2 : 1);
            courseProgress.setLastStudyTime(LocalDateTime.now());
            userCourseProgressMapper.insert(courseProgress);
        } else {
            courseProgress.setProgress(progressValue);
            courseProgress.setStatus(progressValue.compareTo(BigDecimal.valueOf(100)) >= 0 ? 2 : 1);
            courseProgress.setLastStudyTime(LocalDateTime.now());
            userCourseProgressMapper.updateById(courseProgress);
        }
    }
}
