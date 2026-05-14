package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.ChapterDetailResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.ChapterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/{id}")
    public Result<ChapterDetailResponse> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(chapterService.getChapterDetail(id, userId));
    }

    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        chapterService.completeChapter(id, userId);
        return Result.success();
    }
}
