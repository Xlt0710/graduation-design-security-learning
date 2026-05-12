package com.security.learn.service;

import com.security.learn.dto.LabAttemptResponse;
import com.security.learn.dto.LabDetailResponse;
import com.security.learn.dto.LabListResponse;
import com.security.learn.dto.LabSubmitResponse;

import java.util.List;

public interface LabService {

    List<LabListResponse> getLabList(String vulnerabilityType, Integer difficulty, Long userId);

    LabDetailResponse getLabDetail(Long labId, Long userId);

    LabSubmitResponse submitFlag(Long labId, String flag, Long userId);

    String getHint(Long labId, Long userId);

    void favorite(Long labId, Long userId);

    void unfavorite(Long labId, Long userId);

    List<LabAttemptResponse> getAttempts(Long userId);
}
