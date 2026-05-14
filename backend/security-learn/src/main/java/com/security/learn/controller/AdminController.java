package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.NoticeRequest;
import com.security.learn.dto.NoticeResponse;
import com.security.learn.dto.TagRequest;
import com.security.learn.dto.TagResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.AdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/notices")
    public Result<List<NoticeResponse>> getNotices() {
        return Result.success(adminService.getNotices());
    }

    @PostMapping("/notices")
    public Result<NoticeResponse> createNotice(@RequestBody NoticeRequest request) {
        Long userId = requireUserId();
        return Result.success(adminService.createNotice(userId, request));
    }

    @PutMapping("/notices/{id}")
    public Result<NoticeResponse> updateNotice(@PathVariable Long id, @RequestBody NoticeRequest request) {
        Long userId = requireUserId();
        return Result.success(adminService.updateNotice(id, userId, request));
    }

    @DeleteMapping("/notices/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        Long userId = requireUserId();
        adminService.deleteNotice(id, userId);
        return Result.success();
    }

    @GetMapping("/tags")
    public Result<List<TagResponse>> getTags(@RequestParam(required = false) String tagType) {
        return Result.success(adminService.getTags(tagType));
    }

    @PostMapping("/tags")
    public Result<TagResponse> createTag(@RequestBody TagRequest request) {
        Long userId = requireUserId();
        return Result.success(adminService.createTag(userId, request));
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        Long userId = requireUserId();
        adminService.deleteTag(id, userId);
        return Result.success();
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
