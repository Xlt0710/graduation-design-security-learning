package com.security.learn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.learn.dto.QuizDetailResponse;
import com.security.learn.dto.QuizRecordResponse;
import com.security.learn.dto.QuizSubmitResponse;
import com.security.learn.entity.Quiz;
import com.security.learn.entity.QuizQuestion;
import com.security.learn.entity.QuizRecord;
import com.security.learn.mapper.QuizMapper;
import com.security.learn.mapper.QuizQuestionMapper;
import com.security.learn.mapper.QuizRecordMapper;
import com.security.learn.service.QuizService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;
    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizRecordMapper quizRecordMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizServiceImpl(QuizMapper quizMapper, QuizQuestionMapper quizQuestionMapper,
                           QuizRecordMapper quizRecordMapper) {
        this.quizMapper = quizMapper;
        this.quizQuestionMapper = quizQuestionMapper;
        this.quizRecordMapper = quizRecordMapper;
    }

    @Override
    public QuizDetailResponse getQuizDetail(Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("测验不存在");
        }

        List<QuizQuestion> questions = quizQuestionMapper.selectList(
                new LambdaQueryWrapper<QuizQuestion>().eq(QuizQuestion::getQuizId, quizId));

        QuizDetailResponse resp = new QuizDetailResponse();
        resp.setId(quiz.getId());
        resp.setCourseId(quiz.getCourseId());
        resp.setChapterId(quiz.getChapterId());
        resp.setTitle(quiz.getTitle());
        resp.setTotalScore(questions.stream().mapToInt(QuizQuestion::getScore).sum());

        List<QuizDetailResponse.QuestionItem> items = new ArrayList<>();
        for (QuizQuestion q : questions) {
            QuizDetailResponse.QuestionItem item = new QuizDetailResponse.QuestionItem();
            item.setId(q.getId());
            item.setQuestionType(q.getQuestionType());
            item.setContent(q.getContent());
            item.setOptionsJson(q.getOptionsJson());
            item.setScore(q.getScore());
            items.add(item);
        }
        resp.setQuestions(items);
        return resp;
    }

    @Override
    @Transactional
    public QuizSubmitResponse submitQuiz(Long quizId, Map<Long, String> answers, Long userId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new IllegalArgumentException("测验不存在");
        }

        List<QuizQuestion> questions = quizQuestionMapper.selectList(
                new LambdaQueryWrapper<QuizQuestion>().eq(QuizQuestion::getQuizId, quizId));

        int totalScore = 0;
        int userScore = 0;
        List<QuizSubmitResponse.QuestionResult> results = new ArrayList<>();

        for (QuizQuestion q : questions) {
            String userAnswer = answers.getOrDefault(q.getId(), "");
            String correctAnswer = q.getAnswerJson();
            boolean isCorrect = checkAnswer(userAnswer, correctAnswer, q.getQuestionType());

            totalScore += q.getScore();
            if (isCorrect) {
                userScore += q.getScore();
            }

            QuizSubmitResponse.QuestionResult result = new QuizSubmitResponse.QuestionResult();
            result.setQuestionId(q.getId());
            result.setCorrect(isCorrect);
            result.setUserAnswer(userAnswer);
            result.setCorrectAnswer(correctAnswer);
            result.setAnalysis(q.getAnalysis());
            result.setScore(isCorrect ? q.getScore() : 0);
            results.add(result);
        }

        QuizRecord record = new QuizRecord();
        record.setUserId(userId);
        record.setQuizId(quizId);
        record.setScore(userScore);
        try {
            record.setAnswerJson(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            record.setAnswerJson("{}");
        }
        record.setSubmittedAt(LocalDateTime.now());
        quizRecordMapper.insert(record);

        QuizSubmitResponse resp = new QuizSubmitResponse();
        resp.setScore(userScore);
        resp.setTotalScore(totalScore);
        resp.setResults(results);
        return resp;
    }

    @Override
    public List<QuizRecordResponse> getRecords(Long userId) {
        List<QuizRecord> records = quizRecordMapper.selectList(
                new LambdaQueryWrapper<QuizRecord>()
                        .eq(QuizRecord::getUserId, userId)
                        .orderByDesc(QuizRecord::getSubmittedAt));

        List<QuizRecordResponse> result = new ArrayList<>();
        for (QuizRecord r : records) {
            QuizRecordResponse item = new QuizRecordResponse();
            item.setId(r.getId());
            item.setQuizId(r.getQuizId());
            item.setScore(r.getScore());
            item.setSubmittedAt(r.getSubmittedAt());

            Quiz quiz = quizMapper.selectById(r.getQuizId());
            item.setQuizTitle(quiz != null ? quiz.getTitle() : "");

            result.add(item);
        }
        return result;
    }

    private boolean checkAnswer(String userAnswer, String correctAnswer, Integer questionType) {
        if (userAnswer == null || userAnswer.isEmpty()) {
            return false;
        }
        if (questionType == 2) {
            try {
                TypeReference<List<String>> typeRef = new TypeReference<>() {};
                List<String> userList = objectMapper.readValue(userAnswer, typeRef);
                List<String> correctList = objectMapper.readValue(correctAnswer, typeRef);
                return userList.size() == correctList.size() && userList.containsAll(correctList);
            } catch (Exception e) {
                return false;
            }
        }
        return userAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
    }
}
