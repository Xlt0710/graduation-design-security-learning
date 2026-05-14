package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.dto.ChatRequest;
import com.security.learn.dto.ChatResponse;
import com.security.learn.dto.ConversationHistoryResponse;
import com.security.learn.dto.RecommendationResponse;
import com.security.learn.dto.RecommendationResponse.RecommendedItem;
import com.security.learn.entity.AiConversation;
import com.security.learn.entity.AiRecommendation;
import com.security.learn.entity.Course;
import com.security.learn.entity.Lab;
import com.security.learn.entity.LabAttempt;
import com.security.learn.entity.UserCourseProgress;
import com.security.learn.mapper.AiConversationMapper;
import com.security.learn.mapper.AiRecommendationMapper;
import com.security.learn.mapper.CourseMapper;
import com.security.learn.mapper.LabAttemptMapper;
import com.security.learn.mapper.LabMapper;
import com.security.learn.mapper.UserCourseProgressMapper;
import com.security.learn.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final CourseMapper courseMapper;
    private final LabMapper labMapper;
    private final UserCourseProgressMapper userCourseProgressMapper;
    private final LabAttemptMapper labAttemptMapper;
    private final AiRecommendationMapper aiRecommendationMapper;
    private final AiConversationMapper aiConversationMapper;

    public AiServiceImpl(CourseMapper courseMapper, LabMapper labMapper,
                         UserCourseProgressMapper userCourseProgressMapper,
                         LabAttemptMapper labAttemptMapper,
                         AiRecommendationMapper aiRecommendationMapper,
                         AiConversationMapper aiConversationMapper) {
        this.courseMapper = courseMapper;
        this.labMapper = labMapper;
        this.userCourseProgressMapper = userCourseProgressMapper;
        this.labAttemptMapper = labAttemptMapper;
        this.aiRecommendationMapper = aiRecommendationMapper;
        this.aiConversationMapper = aiConversationMapper;
    }

    @Override
    public RecommendationResponse getRecommendations(Long userId) {
        List<AiRecommendation> existing = aiRecommendationMapper.selectList(
                new LambdaQueryWrapper<AiRecommendation>()
                        .eq(AiRecommendation::getUserId, userId)
                        .orderByDesc(AiRecommendation::getCreatedAt));

        if (!existing.isEmpty()) {
            return buildResponse(existing);
        }
        return refreshRecommendations(userId);
    }

    @Override
    @Transactional
    public RecommendationResponse refreshRecommendations(Long userId) {
        aiRecommendationMapper.delete(new LambdaQueryWrapper<AiRecommendation>()
                .eq(AiRecommendation::getUserId, userId));

        Map<String, Double> weakAreas = analyzeWeakAreas(userId);
        Set<Long> userCourseIds = getUserCourseIds(userId);
        Set<Long> attemptedLabIds = getAttemptedLabIds(userId);

        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1));
        List<Lab> labs = labMapper.selectList(
                new LambdaQueryWrapper<Lab>().eq(Lab::getStatus, 1));

        List<RecommendedItem> courseItems = new ArrayList<>();
        for (Course course : courses) {
            if (userCourseIds.contains(course.getId())) {
                continue;
            }
            BigDecimal score = scoreCourse(course, weakAreas);
            if (score.compareTo(BigDecimal.ZERO) > 0) {
                String reason = generateCourseReason(course, weakAreas);
                courseItems.add(new RecommendedItem(
                        course.getId(), course.getTitle(), reason, score, "COURSE", course.getDifficulty()));
                saveRecommendation(userId, "COURSE", course.getId(), reason, score);
            }
        }

        List<RecommendedItem> labItems = new ArrayList<>();
        for (Lab lab : labs) {
            BigDecimal score = scoreLab(lab, weakAreas, attemptedLabIds, userId);
            if (score.compareTo(BigDecimal.ZERO) > 0) {
                String reason = generateLabReason(lab, weakAreas, attemptedLabIds);
                labItems.add(new RecommendedItem(
                        lab.getId(), lab.getTitle(), reason, score, "LAB", lab.getDifficulty()));
                saveRecommendation(userId, "LAB", lab.getId(), reason, score);
            }
        }

        courseItems.sort(Comparator.comparing(RecommendedItem::getScore).reversed());
        labItems.sort(Comparator.comparing(RecommendedItem::getScore).reversed());

        String suggestion = generateSuggestion(weakAreas, courseItems, labItems);

        RecommendationResponse response = new RecommendationResponse();
        response.setCourses(courseItems);
        response.setLabs(labItems);
        response.setSuggestion(suggestion);
        return response;
    }

    @Override
    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        String reply = generateChatReply(request.getMessage());

        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setScene(request.getScene() != null ? request.getScene() : "general");
        conversation.setPrompt(request.getMessage());
        conversation.setResponse(reply);
        conversation.setCreatedAt(LocalDateTime.now());
        aiConversationMapper.insert(conversation);

        log.info("AI对话: userId={}, scene={}", userId, conversation.getScene());
        return new ChatResponse(reply, conversation.getId());
    }

    @Override
    public List<ConversationHistoryResponse> getConversations(Long userId) {
        List<AiConversation> conversations = aiConversationMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getCreatedAt));
        return conversations.stream()
                .map(c -> new ConversationHistoryResponse(
                        c.getId(), c.getScene(), c.getPrompt(), c.getResponse(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private RecommendationResponse buildResponse(List<AiRecommendation> records) {
        List<RecommendedItem> courses = new ArrayList<>();
        List<RecommendedItem> labs = new ArrayList<>();

        Map<Long, Course> courseMap = courseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Map<Long, Lab> labMap = labMapper.selectList(null).stream()
                .collect(Collectors.toMap(Lab::getId, l -> l));

        for (AiRecommendation r : records) {
            if ("COURSE".equals(r.getRecommendType())) {
                Course course = courseMap.get(r.getTargetId());
                String title = course != null ? course.getTitle() : "未知课程";
                Integer difficulty = course != null ? course.getDifficulty() : null;
                courses.add(new RecommendedItem(
                        r.getTargetId(), title, r.getReason(), r.getScore(), "COURSE", difficulty));
            } else {
                Lab lab = labMap.get(r.getTargetId());
                String title = lab != null ? lab.getTitle() : "未知题目";
                Integer difficulty = lab != null ? lab.getDifficulty() : null;
                labs.add(new RecommendedItem(
                        r.getTargetId(), title, r.getReason(), r.getScore(), "LAB", difficulty));
            }
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setCourses(courses);
        response.setLabs(labs);
        response.setSuggestion("基于你的学习进度，以上是为你推荐的学习内容。");
        return response;
    }

    private Map<String, Double> analyzeWeakAreas(Long userId) {
        List<LabAttempt> attempts = labAttemptMapper.selectList(
                new LambdaQueryWrapper<LabAttempt>().eq(LabAttempt::getUserId, userId));
        if (attempts.isEmpty()) {
            return Map.of();
        }

        List<Lab> allLabs = labMapper.selectList(null);
        Map<Long, String> labTypeMap = allLabs.stream()
                .collect(Collectors.toMap(Lab::getId, Lab::getVulnerabilityType));

        Map<String, List<LabAttempt>> byType = attempts.stream()
                .filter(a -> labTypeMap.containsKey(a.getLabId()))
                .collect(Collectors.groupingBy(a -> labTypeMap.get(a.getLabId())));

        Map<String, Double> weakAreas = new java.util.HashMap<>();
        for (Map.Entry<String, List<LabAttempt>> entry : byType.entrySet()) {
            long correct = entry.getValue().stream().filter(a -> a.getIsCorrect() == 1).count();
            double rate = (double) correct / entry.getValue().size();
            if (rate < 0.6) {
                weakAreas.put(entry.getKey(), rate);
            }
        }
        return weakAreas;
    }

    private Set<Long> getUserCourseIds(Long userId) {
        return userCourseProgressMapper.selectList(
                new LambdaQueryWrapper<UserCourseProgress>().eq(UserCourseProgress::getUserId, userId))
                .stream().map(UserCourseProgress::getCourseId).collect(Collectors.toSet());
    }

    private Set<Long> getAttemptedLabIds(Long userId) {
        return labAttemptMapper.selectList(
                new LambdaQueryWrapper<LabAttempt>().eq(LabAttempt::getUserId, userId))
                .stream().map(LabAttempt::getLabId).collect(Collectors.toSet());
    }

    private BigDecimal scoreCourse(Course course, Map<String, Double> weakAreas) {
        double score = 50.0;
        String type = matchCourseToVulnType(course.getTitle());
        if (type != null && weakAreas.containsKey(type)) {
            score += (1.0 - weakAreas.get(type)) * 30;
        }
        score += (4 - course.getDifficulty()) * 5;
        return BigDecimal.valueOf(Math.min(score, 100)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scoreLab(Lab lab, Map<String, Double> weakAreas,
                                 Set<Long> attemptedLabIds, Long userId) {
        double score = 40.0;
        if (weakAreas.containsKey(lab.getVulnerabilityType())) {
            score += (1.0 - weakAreas.get(lab.getVulnerabilityType())) * 40;
        }
        if (!attemptedLabIds.contains(lab.getId())) {
            score += 15;
        } else {
            boolean lastFailed = labAttemptMapper.selectList(
                    new LambdaQueryWrapper<LabAttempt>()
                            .eq(LabAttempt::getUserId, userId)
                            .eq(LabAttempt::getLabId, lab.getId())
                            .orderByDesc(LabAttempt::getSubmittedAt)
                            .last("LIMIT 1"))
                    .stream().anyMatch(a -> a.getIsCorrect() == 0);
            if (lastFailed) {
                score += 10;
            }
        }
        score += (4 - lab.getDifficulty()) * 3;
        return BigDecimal.valueOf(Math.min(score, 100)).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateCourseReason(Course course, Map<String, Double> weakAreas) {
        String type = matchCourseToVulnType(course.getTitle());
        if (type != null && weakAreas.containsKey(type)) {
            return String.format("你在 %s 类型题目上正确率偏低，建议学习相关课程巩固基础。", formatVulnType(type));
        }
        return "根据你的学习进度，这门课程适合当前阶段学习。";
    }

    private String generateLabReason(Lab lab, Map<String, Double> weakAreas,
                                      Set<Long> attemptedLabIds) {
        if (weakAreas.containsKey(lab.getVulnerabilityType())) {
            return String.format("你在 %s 类型上正确率为 %.0f%%，建议多加练习。",
                    formatVulnType(lab.getVulnerabilityType()),
                    weakAreas.get(lab.getVulnerabilityType()) * 100);
        }
        if (!attemptedLabIds.contains(lab.getId())) {
            return "新题目，来挑战一下吧！";
        }
        return "这道题值得再试一次。";
    }

    private String generateSuggestion(Map<String, Double> weakAreas,
                                       List<RecommendedItem> courses,
                                       List<RecommendedItem> labs) {
        StringBuilder sb = new StringBuilder();
        if (weakAreas.isEmpty()) {
            sb.append("你目前没有明显的薄弱环节，建议按课程顺序系统学习。");
        } else {
            sb.append("根据你的练习记录，");
            for (Map.Entry<String, Double> entry : weakAreas.entrySet()) {
                sb.append(String.format("「%s」类型正确率较低（%.0f%%），",
                        formatVulnType(entry.getKey()), entry.getValue() * 100));
            }
            sb.append("建议优先学习相关课程并完成对应靶场练习。");
        }
        if (!courses.isEmpty()) {
            sb.append(String.format(" 为你推荐了 %d 门课程", courses.size()));
        }
        if (!labs.isEmpty()) {
            sb.append(String.format("和 %d 个靶场题目。", labs.size()));
        }
        return sb.toString();
    }

    private String generateChatReply(String message) {
        String msg = message.toLowerCase();
        if (msg.contains("sql") || msg.contains("注入")) {
            return "SQL注入是常见的Web安全漏洞。攻击者通过在输入中插入恶意SQL语句来操纵数据库。"
                    + "建议先学习「SQL注入基础」课程，然后完成对应的靶场练习。"
                    + "关键防护方法：使用参数化查询、输入校验、最小权限原则。";
        }
        if (msg.contains("xss") || msg.contains("跨站")) {
            return "XSS（跨站脚本攻击）分为反射型、存储型和DOM型三种。"
                    + "攻击者将恶意脚本注入页面，在受害者浏览器中执行。"
                    + "防护方法：输出编码、CSP策略、HttpOnly Cookie。"
                    + "推荐学习「XSS跨站脚本基础」课程。";
        }
        if (msg.contains("csrf")) {
            return "CSRF（跨站请求伪造）利用用户已登录的身份，在用户不知情的情况下发起恶意请求。"
                    + "防护方法：CSRF Token、SameSite Cookie、Referer校验。";
        }
        if (msg.contains("密码") || msg.contains("认证") || msg.contains("auth")) {
            return "在Web安全中，认证和授权是基础。常见问题包括：弱密码、暴力破解、会话劫持。"
                    + "建议：使用强密码策略、多因素认证、安全的会话管理、JWT最佳实践。";
        }
        if (msg.contains("学习路线") || msg.contains("学习路径") || msg.contains("怎么学")) {
            return "推荐Web安全学习路线：\n"
                    + "1. 基础：HTTP协议、HTML/JS基础\n"
                    + "2. 入门漏洞：SQL注入 → XSS → CSRF\n"
                    + "3. 进阶：文件上传 → 命令注入 → SSRF\n"
                    + "4. 高级：反序列化 → XXE → 权限提升\n"
                    + "建议跟随平台的课程顺序学习，边学边练效果最好。";
        }
        return "你好！我是AI学习助手。你可以问我关于Web安全的问题，比如SQL注入、XSS、CSRF等漏洞的原理和防护方法。"
                + "也可以让我为你推荐学习路线。";
    }

    private void saveRecommendation(Long userId, String type, Long targetId, String reason, BigDecimal score) {
        AiRecommendation rec = new AiRecommendation();
        rec.setUserId(userId);
        rec.setRecommendType(type);
        rec.setTargetId(targetId);
        rec.setReason(reason);
        rec.setScore(score);
        rec.setCreatedAt(LocalDateTime.now());
        aiRecommendationMapper.insert(rec);
    }

    private String matchCourseToVulnType(String title) {
        if (title == null) return null;
        String t = title.toLowerCase();
        if (t.contains("sql")) return "SQL_INJECTION";
        if (t.contains("xss")) return "XSS";
        if (t.contains("csrf")) return "CSRF";
        if (t.contains("文件上传") || t.contains("upload")) return "FILE_UPLOAD";
        if (t.contains("命令注入") || t.contains("command")) return "COMMAND_INJECTION";
        return null;
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
