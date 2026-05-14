package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.dto.ReportGenerateRequest;
import com.security.learn.dto.ReportListResponse;
import com.security.learn.dto.ReportRequest;
import com.security.learn.dto.ReportResponse;
import com.security.learn.entity.Lab;
import com.security.learn.entity.VulnerabilityReport;
import com.security.learn.mapper.LabMapper;
import com.security.learn.mapper.VulnerabilityReportMapper;
import com.security.learn.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final VulnerabilityReportMapper reportMapper;
    private final LabMapper labMapper;

    public ReportServiceImpl(VulnerabilityReportMapper reportMapper, LabMapper labMapper) {
        this.reportMapper = reportMapper;
        this.labMapper = labMapper;
    }

    @Override
    public List<ReportListResponse> getReports(Long userId) {
        return reportMapper.selectList(
                new LambdaQueryWrapper<VulnerabilityReport>()
                        .eq(VulnerabilityReport::getUserId, userId)
                        .orderByDesc(VulnerabilityReport::getCreatedAt))
                .stream()
                .map(r -> new ReportListResponse(
                        r.getId(), r.getTitle(), r.getVulnerabilityType(),
                        r.getRiskLevel(), r.getAiGenerated(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public ReportResponse getReport(Long reportId, Long userId) {
        VulnerabilityReport report = reportMapper.selectById(reportId);
        if (report == null || !report.getUserId().equals(userId)) {
            throw new IllegalArgumentException("报告不存在");
        }
        return toResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse createReport(Long userId, ReportRequest request) {
        VulnerabilityReport report = new VulnerabilityReport();
        report.setUserId(userId);
        report.setTitle(request.getTitle());
        report.setVulnerabilityType(request.getVulnerabilityType());
        report.setRiskLevel(request.getRiskLevel());
        report.setDescription(request.getDescription());
        report.setReproduceSteps(request.getReproduceSteps());
        report.setImpact(request.getImpact());
        report.setRepairSuggestion(request.getRepairSuggestion());
        report.setAiGenerated(0);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.insert(report);

        log.info("漏洞报告创建: id={}, userId={}", report.getId(), userId);
        return toResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse updateReport(Long reportId, Long userId, ReportRequest request) {
        VulnerabilityReport report = reportMapper.selectById(reportId);
        if (report == null || !report.getUserId().equals(userId)) {
            throw new IllegalArgumentException("报告不存在");
        }

        if (StringUtils.hasText(request.getTitle())) report.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getVulnerabilityType())) report.setVulnerabilityType(request.getVulnerabilityType());
        if (StringUtils.hasText(request.getRiskLevel())) report.setRiskLevel(request.getRiskLevel());
        if (request.getDescription() != null) report.setDescription(request.getDescription());
        if (request.getReproduceSteps() != null) report.setReproduceSteps(request.getReproduceSteps());
        if (request.getImpact() != null) report.setImpact(request.getImpact());
        if (request.getRepairSuggestion() != null) report.setRepairSuggestion(request.getRepairSuggestion());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        return toResponse(report);
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        VulnerabilityReport report = reportMapper.selectById(reportId);
        if (report == null || !report.getUserId().equals(userId)) {
            throw new IllegalArgumentException("报告不存在");
        }
        reportMapper.deleteById(reportId);
    }

    @Override
    @Transactional
    public ReportResponse generateReport(Long userId, ReportGenerateRequest request) {
        Lab lab = null;
        if (request.getLabId() != null) {
            lab = labMapper.selectById(request.getLabId());
        }

        String vulnType = lab != null ? lab.getVulnerabilityType() : "未知";
        String riskLevel = lab != null && lab.getDifficulty() >= 3 ? "高危" : "中危";

        String title = lab != null ? lab.getTitle() + " - 漏洞分析报告" : "漏洞分析报告";
        String description = generateDescription(lab, vulnType);
        String reproduceSteps = generateReproduceSteps(lab, vulnType);
        String impact = generateImpact(vulnType);
        String repairSuggestion = generateRepairSuggestion(vulnType);
        if (StringUtils.hasText(request.getNotes())) {
            description += "\n\n用户备注：" + request.getNotes();
        }

        VulnerabilityReport report = new VulnerabilityReport();
        report.setUserId(userId);
        report.setLabId(request.getLabId());
        report.setTitle(title);
        report.setVulnerabilityType(vulnType);
        report.setRiskLevel(riskLevel);
        report.setDescription(description);
        report.setReproduceSteps(reproduceSteps);
        report.setImpact(impact);
        report.setRepairSuggestion(repairSuggestion);
        report.setAiGenerated(1);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.insert(report);

        log.info("AI生成漏洞报告: id={}, labId={}", report.getId(), request.getLabId());
        return toResponse(report);
    }

    private ReportResponse toResponse(VulnerabilityReport r) {
        String labTitle = null;
        if (r.getLabId() != null) {
            Lab lab = labMapper.selectById(r.getLabId());
            if (lab != null) labTitle = lab.getTitle();
        }
        return new ReportResponse(
                r.getId(), r.getLabId(), labTitle, r.getTitle(), r.getVulnerabilityType(),
                r.getRiskLevel(), r.getDescription(), r.getReproduceSteps(),
                r.getImpact(), r.getRepairSuggestion(), r.getAiGenerated(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    private String generateDescription(Lab lab, String vulnType) {
        if (lab != null) {
            return String.format("本文档是针对靶场「%s」中发现的 %s 漏洞的详细分析报告。\n\n"
                    + "靶场描述：%s\n\n"
                    + "漏洞类型：%s",
                    lab.getTitle(), formatVulnType(vulnType), lab.getDescription(), formatVulnType(vulnType));
        }
        return "本报告由AI辅助生成，提供了对目标漏洞的全面分析。";
    }

    private String generateReproduceSteps(Lab lab, String vulnType) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 复现步骤\n\n");
        sb.append("1. 访问靶场环境");
        if (lab != null && lab.getTargetUrl() != null) {
            sb.append("：").append(lab.getTargetUrl());
        }
        sb.append("\n");
        sb.append("2. 分析目标页面的输入点和功能点\n");
        sb.append("3. 构造测试payload并输入\n");
        sb.append("4. 观察返回结果，验证漏洞是否存在\n");
        sb.append("5. 根据验证结果进行深入利用测试\n");
        if (StringUtils.hasText(lab.getHint())) {
            sb.append("\n提示：").append(lab.getHint()).append("\n");
        }
        return sb.toString();
    }

    private String generateImpact(String vulnType) {
        return switch (vulnType) {
            case "SQL_INJECTION" -> "攻击者可能通过SQL注入获取数据库中的敏感信息（用户名、密码、个人数据），"
                    + "甚至删除或篡改数据，导致数据泄露和业务中断。";
            case "XSS" -> "攻击者可在受害者浏览器中执行恶意脚本，窃取Cookie、会话令牌，"
                    + "实施钓鱼攻击，或篡改页面内容。";
            case "CSRF" -> "攻击者可利用用户已登录的身份，在用户不知情的情况下发起恶意请求，"
                    + "如修改密码、转账、发布恶意内容等。";
            default -> "该漏洞可能导致未授权访问、数据泄露或系统被控制，"
                    + "具体影响程度取决于漏洞利用的深度和攻击者的意图。";
        };
    }

    private String generateRepairSuggestion(String vulnType) {
        return switch (vulnType) {
            case "SQL_INJECTION" -> "## 修复建议\n\n"
                    + "1. **使用参数化查询（Prepared Statement）**：所有数据库操作使用预编译语句\n"
                    + "2. **输入校验**：对用户输入进行严格的类型检查和格式验证\n"
                    + "3. **最小权限原则**：数据库连接使用最小必要权限\n"
                    + "4. **错误信息处理**：避免向用户暴露数据库错误详情\n"
                    + "5. **WAF配置**：部署Web应用防火墙，过滤SQL注入特征";
            case "XSS" -> "## 修复建议\n\n"
                    + "1. **输出编码**：所有用户输入在输出到页面时进行HTML实体编码\n"
                    + "2. **CSP策略**：设置Content-Security-Policy头，限制脚本来源\n"
                    + "3. **HttpOnly Cookie**：为敏感Cookie设置HttpOnly标志\n"
                    + "4. **输入过滤**：使用白名单机制过滤用户提交的内容\n"
                    + "5. **XSS过滤器**：启用框架自带的XSS防护机制";
            case "CSRF" -> "## 修复建议\n\n"
                    + "1. **CSRF Token**：在每个表单和敏感请求中包含随机Token\n"
                    + "2. **SameSite Cookie**：设置Cookie的SameSite属性为Strict或Lax\n"
                    + "3. **Referer/Origin校验**：验证请求来源是否合法\n"
                    + "4. **二次验证**：对敏感操作要求输入密码或验证码";
            default -> "## 修复建议\n\n"
                    + "1. 对用户输入进行严格的验证和过滤\n"
                    + "2. 遵循最小权限原则\n"
                    + "3. 保持系统和依赖库更新到最新版本\n"
                    + "4. 定期进行安全审计和渗透测试";
        };
    }

    private String formatVulnType(String type) {
        if (type == null) return "未知";
        return switch (type) {
            case "SQL_INJECTION" -> "SQL注入";
            case "XSS" -> "XSS跨站脚本";
            case "CSRF" -> "CSRF";
            case "FILE_UPLOAD" -> "文件上传";
            case "COMMAND_INJECTION" -> "命令注入";
            default -> type;
        };
    }
}
