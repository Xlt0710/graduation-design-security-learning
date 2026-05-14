package com.security.learn.service;

import com.security.learn.dto.ReportGenerateRequest;
import com.security.learn.dto.ReportListResponse;
import com.security.learn.dto.ReportRequest;
import com.security.learn.dto.ReportResponse;

import java.util.List;

public interface ReportService {

    List<ReportListResponse> getReports(Long userId);

    ReportResponse getReport(Long reportId, Long userId);

    ReportResponse createReport(Long userId, ReportRequest request);

    ReportResponse updateReport(Long reportId, Long userId, ReportRequest request);

    void deleteReport(Long reportId, Long userId);

    ReportResponse generateReport(Long userId, ReportGenerateRequest request);
}
