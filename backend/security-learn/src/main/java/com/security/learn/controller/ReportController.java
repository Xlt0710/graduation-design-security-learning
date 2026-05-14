package com.security.learn.controller;

import com.security.learn.common.Result;
import com.security.learn.dto.ReportGenerateRequest;
import com.security.learn.dto.ReportListResponse;
import com.security.learn.dto.ReportRequest;
import com.security.learn.dto.ReportResponse;
import com.security.learn.security.SecurityUtils;
import com.security.learn.service.ReportService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public Result<List<ReportListResponse>> list() {
        Long userId = requireUserId();
        return Result.success(reportService.getReports(userId));
    }

    @GetMapping("/reports/{id}")
    public Result<ReportResponse> detail(@PathVariable Long id) {
        Long userId = requireUserId();
        return Result.success(reportService.getReport(id, userId));
    }

    @PostMapping("/reports")
    public Result<ReportResponse> create(@RequestBody ReportRequest request) {
        Long userId = requireUserId();
        return Result.success(reportService.createReport(userId, request));
    }

    @PutMapping("/reports/{id}")
    public Result<ReportResponse> update(@PathVariable Long id, @RequestBody ReportRequest request) {
        Long userId = requireUserId();
        return Result.success(reportService.updateReport(id, userId, request));
    }

    @DeleteMapping("/reports/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = requireUserId();
        reportService.deleteReport(id, userId);
        return Result.success();
    }

    @PostMapping("/ai/reports/generate")
    public Result<ReportResponse> generateReport(@RequestBody ReportGenerateRequest request) {
        Long userId = requireUserId();
        return Result.success(reportService.generateReport(userId, request));
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("请先登录");
        }
        return userId;
    }
}
