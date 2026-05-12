package com.security.learn.service;

import com.security.learn.dto.ChapterDetailResponse;

public interface ChapterService {

    ChapterDetailResponse getChapterDetail(Long chapterId, Long userId);

    void completeChapter(Long chapterId, Long userId);
}
