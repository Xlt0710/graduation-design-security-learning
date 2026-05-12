package com.security.learn.service;

import com.security.learn.dto.QuizDetailResponse;
import com.security.learn.dto.QuizRecordResponse;
import com.security.learn.dto.QuizSubmitResponse;

import java.util.List;
import java.util.Map;

public interface QuizService {

    QuizDetailResponse getQuizDetail(Long quizId);

    QuizSubmitResponse submitQuiz(Long quizId, Map<Long, String> answers, Long userId);

    List<QuizRecordResponse> getRecords(Long userId);
}
