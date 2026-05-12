package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.ChapterDetailResponse;
import com.security.learn.service.ChapterService;
import com.security.learn.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    private final ChapterService chapterService;
    private final UserService userService;

    public ChapterController(ChapterService chapterService, UserService userService) {
        this.chapterService = chapterService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<ChapterDetailResponse> detail(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return Result.success(chapterService.getChapterDetail(id, userId));
    }

    @PostMapping("/{id}/complete")
    public Result<Void> complete(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        chapterService.completeChapter(id, userId);
        return Result.success();
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userService.getUserIdByUsername(authentication.getName());
    }
}
