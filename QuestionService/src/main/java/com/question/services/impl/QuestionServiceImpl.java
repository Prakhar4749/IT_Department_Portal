package com.question.services.impl;

import com.question.entities.Question;
import com.question.repositories.QuestionRepository;
import com.question.services.QuestionService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class QuestionServiceImpl implements QuestionService {

    private QuestionRepository questionRepo;

    public QuestionServiceImpl(QuestionRepository questionRepo) {
        this.questionRepo = questionRepo;
    }

    @Override
    public Question add(Question question) {
        return questionRepo.save(question);
    }

    @Override
    public List<Question> get() {
        return questionRepo.findAll();
    }

    @Override
    public Question get(Long id) {
        return questionRepo.findById(id).orElseThrow(() -> new RuntimeException("Question not found"));
    }

    @Override
    public List<Question> getQuestionOfQuizId(Long quizId) {
        return questionRepo.findByQuizId(quizId);
    }
}
