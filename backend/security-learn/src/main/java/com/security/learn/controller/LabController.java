package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.LabAttemptResponse;
import com.security.learn.dto.LabDetailResponse;
import com.security.learn.dto.LabListResponse;
import com.security.learn.dto.LabSubmitRequest;
import com.security.learn.dto.LabSubmitResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.LabService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labs")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping
    public Result<List<LabListResponse>> list(
            @RequestParam(required = false) String vulnerabilityType,
            @RequestParam(required = false) Integer difficulty) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(labService.getLabList(vulnerabilityType, difficulty, userId));
    }

    @GetMapping("/{id}")
    public Result<LabDetailResponse> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(labService.getLabDetail(id, userId));
    }

    @PostMapping("/{id}/submit")
    public Result<LabSubmitResponse> submit(
            @PathVariable Long id,
            @RequestBody LabSubmitRequest request) {
        Long userId = requireUserId();
        return Result.success(labService.submitFlag(id, request.getFlag(), userId));
    }

    @PostMapping("/{id}/hint")
    public Result<Map<String, String>> hint(@PathVariable Long id) {
        Long userId = requireUserId();
        String hint = labService.getHint(id, userId);
        return Result.success(Map.of("hint", hint));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        Long userId = requireUserId();
        labService.favorite(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavorite(@PathVariable Long id) {
        Long userId = requireUserId();
        labService.unfavorite(id, userId);
        return Result.success();
    }

    @GetMapping("/attempts")
    public Result<List<LabAttemptResponse>> attempts() {
        Long userId = requireUserId();
        return Result.success(labService.getAttempts(userId));
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
