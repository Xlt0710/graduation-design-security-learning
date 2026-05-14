package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.security.learn.dto.LabAttemptResponse;
import com.security.learn.dto.LabDetailResponse;
import com.security.learn.dto.LabListResponse;
import com.security.learn.dto.LabSubmitResponse;
import com.security.learn.entity.Lab;
import com.security.learn.entity.LabAttempt;
import com.security.learn.entity.LabFavorite;
import com.security.learn.mapper.LabAttemptMapper;
import com.security.learn.mapper.LabFavoriteMapper;
import com.security.learn.mapper.LabMapper;
import com.security.learn.service.LabService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LabServiceImpl implements LabService {

    private final LabMapper labMapper;
    private final LabAttemptMapper labAttemptMapper;
    private final LabFavoriteMapper labFavoriteMapper;

    public LabServiceImpl(LabMapper labMapper, LabAttemptMapper labAttemptMapper,
                          LabFavoriteMapper labFavoriteMapper) {
        this.labMapper = labMapper;
        this.labAttemptMapper = labAttemptMapper;
        this.labFavoriteMapper = labFavoriteMapper;
    }

    @Override
    public List<LabListResponse> getLabList(String vulnerabilityType, Integer difficulty, Long userId) {
        LambdaQueryWrapper<Lab> wrapper = new LambdaQueryWrapper<Lab>()
                .eq(Lab::getStatus, 1)
                .orderByAsc(Lab::getDifficulty, Lab::getId);
        if (StringUtils.hasText(vulnerabilityType)) {
            wrapper.eq(Lab::getVulnerabilityType, vulnerabilityType);
        }
        if (difficulty != null) {
            wrapper.eq(Lab::getDifficulty, difficulty);
        }
        List<Lab> labs = labMapper.selectList(wrapper);

        Map<Long, Boolean> solvedMap = Map.of();
        Map<Long, Integer> attemptCountMap = Map.of();
        Map<Long, Boolean> favMap = Map.of();
        if (userId != null) {
            List<LabAttempt> attempts = labAttemptMapper.selectList(
                    new LambdaQueryWrapper<LabAttempt>().eq(LabAttempt::getUserId, userId));
            solvedMap = attempts.stream()
                    .filter(a -> a.getIsCorrect() == 1)
                    .collect(Collectors.toMap(LabAttempt::getLabId, a -> true, (a, b) -> a));
            attemptCountMap = attempts.stream()
                    .collect(Collectors.groupingBy(LabAttempt::getLabId,
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
            List<LabFavorite> favs = labFavoriteMapper.selectList(
                    new LambdaQueryWrapper<LabFavorite>().eq(LabFavorite::getUserId, userId));
            favMap = favs.stream()
                    .collect(Collectors.toMap(LabFavorite::getLabId, f -> true, (a, b) -> a));
        }

        List<LabListResponse> result = new ArrayList<>();
        for (Lab lab : labs) {
            LabListResponse item = new LabListResponse();
            item.setId(lab.getId());
            item.setTitle(lab.getTitle());
            item.setVulnerabilityType(lab.getVulnerabilityType());
            item.setDifficulty(lab.getDifficulty());
            item.setSolved(solvedMap.getOrDefault(lab.getId(), false));
            item.setAttemptCount(attemptCountMap.getOrDefault(lab.getId(), 0));
            item.setFavorited(favMap.getOrDefault(lab.getId(), false));
            result.add(item);
        }
        return result;
    }

    @Override
    public LabDetailResponse getLabDetail(Long labId, Long userId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null || lab.getStatus() == 0) {
            throw new IllegalArgumentException("题目不存在");
        }

        LabDetailResponse resp = new LabDetailResponse();
        resp.setId(lab.getId());
        resp.setTitle(lab.getTitle());
        resp.setDescription(lab.getDescription());
        resp.setVulnerabilityType(lab.getVulnerabilityType());
        resp.setDifficulty(lab.getDifficulty());
        resp.setTargetUrl(lab.getTargetUrl());
        resp.setHint(lab.getHint());

        if (userId == null) {
            resp.setHintUsed(false);
            resp.setSolved(false);
            resp.setAttemptCount(0);
            resp.setFavorited(false);
            return resp;
        }

        List<LabAttempt> attempts = labAttemptMapper.selectList(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .eq(LabAttempt::getLabId, labId));
        resp.setHintUsed(attempts.stream().anyMatch(a -> a.getUsedHint() == 1));
        resp.setSolved(attempts.stream().anyMatch(a -> a.getIsCorrect() == 1));
        resp.setAttemptCount(attempts.size());

        Long favCount = labFavoriteMapper.selectCount(
                new LambdaQueryWrapper<LabFavorite>()
                        .eq(LabFavorite::getUserId, userId)
                        .eq(LabFavorite::getLabId, labId));
        resp.setFavorited(favCount > 0);

        return resp;
    }

    @Override
    @Transactional
    public LabSubmitResponse submitFlag(Long labId, String flag, Long userId) {
        if (!StringUtils.hasText(flag)) {
            throw new IllegalArgumentException("flag不能为空");
        }

        Lab lab = labMapper.selectById(labId);
        if (lab == null || lab.getStatus() == 0) {
            throw new IllegalArgumentException("题目不存在");
        }

        boolean alreadySolved = labAttemptMapper.selectCount(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .eq(LabAttempt::getLabId, labId)
                        .eq(LabAttempt::getIsCorrect, 1)) > 0;

        int existingAttempts = (int) labAttemptMapper.selectCount(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .eq(LabAttempt::getLabId, labId)).longValue();

        boolean correct = lab.getFlag().equals(flag.trim());

        LabAttempt attempt = new LabAttempt();
        attempt.setUserId(userId);
        attempt.setLabId(labId);
        attempt.setSubmittedFlag(flag.trim());
        attempt.setIsCorrect(correct ? 1 : 0);
        attempt.setUsedHint(0);
        attempt.setAttemptCount(existingAttempts + 1);
        attempt.setSubmittedAt(LocalDateTime.now());
        labAttemptMapper.insert(attempt);

        if (correct) {
            if (alreadySolved) {
                return new LabSubmitResponse(true, "回答正确！", existingAttempts + 1);
            }
            return new LabSubmitResponse(true, "恭喜你，回答正确！", existingAttempts + 1);
        }
        return new LabSubmitResponse(false, "回答错误，请再试一次", existingAttempts + 1);
    }

    @Override
    @Transactional
    public String getHint(Long labId, Long userId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null || lab.getStatus() == 0) {
            throw new IllegalArgumentException("题目不存在");
        }

        LabAttempt existing = labAttemptMapper.selectOne(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .eq(LabAttempt::getLabId, labId)
                        .eq(LabAttempt::getUsedHint, 1));
        if (existing != null) {
            return lab.getHint();
        }

        int count = (int) labAttemptMapper.selectCount(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .eq(LabAttempt::getLabId, labId)).longValue();

        LabAttempt attempt = new LabAttempt();
        attempt.setUserId(userId);
        attempt.setLabId(labId);
        attempt.setSubmittedFlag("");
        attempt.setIsCorrect(0);
        attempt.setUsedHint(1);
        attempt.setAttemptCount(count + 1);
        attempt.setSubmittedAt(LocalDateTime.now());
        labAttemptMapper.insert(attempt);

        return lab.getHint();
    }

    @Override
    public void favorite(Long labId, Long userId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        Long count = labFavoriteMapper.selectCount(
                new LambdaQueryWrapper<LabFavorite>()
                        .eq(LabFavorite::getUserId, userId)
                        .eq(LabFavorite::getLabId, labId));
        if (count == 0) {
            LabFavorite fav = new LabFavorite();
            fav.setUserId(userId);
            fav.setLabId(labId);
            fav.setCreatedAt(LocalDateTime.now());
            labFavoriteMapper.insert(fav);
        }
    }

    @Override
    public void unfavorite(Long labId, Long userId) {
        labFavoriteMapper.delete(
                new LambdaQueryWrapper<LabFavorite>()
                        .eq(LabFavorite::getUserId, userId)
                        .eq(LabFavorite::getLabId, labId));
    }

    @Override
    public List<LabAttemptResponse> getAttempts(Long userId) {
        List<LabAttempt> attempts = labAttemptMapper.selectList(
                new LambdaQueryWrapper<LabAttempt>()
                        .eq(LabAttempt::getUserId, userId)
                        .orderByDesc(LabAttempt::getSubmittedAt));

        Map<Long, String> labTitleMap = Map.of();
        if (!attempts.isEmpty()) {
            List<Long> labIds = attempts.stream().map(LabAttempt::getLabId).distinct().toList();
            labTitleMap = labMapper.selectBatchIds(labIds).stream()
                    .collect(Collectors.toMap(Lab::getId, Lab::getTitle));
        }

        List<LabAttemptResponse> result = new ArrayList<>();
        for (LabAttempt a : attempts) {
            LabAttemptResponse item = new LabAttemptResponse();
            item.setId(a.getId());
            item.setLabId(a.getLabId());
            item.setIsCorrect(a.getIsCorrect() == 1);
            item.setUsedHint(a.getUsedHint() == 1);
            item.setAttemptCount(a.getAttemptCount());
            item.setSubmittedAt(a.getSubmittedAt());
            item.setLabTitle(labTitleMap.getOrDefault(a.getLabId(), ""));
            result.add(item);
        }
        return result;
    }
}
