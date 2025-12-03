package com.quiz.services.impl;

import com.quiz.entities.Question;
import com.quiz.entities.Quiz;
import com.quiz.repositories.QuizRepository;
import com.quiz.services.QuestionClient;
import com.quiz.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    @Autowired
    private QuizRepository quizRepo;

    @Autowired
    private QuestionClient questionClient;



    @Override
    public Quiz add(Quiz quiz) {
        return quizRepo.save(quiz);
    }

    @Override
    public List<Quiz> get() {

        List<Quiz> Quizzes = quizRepo.findAll();

        List<Quiz> newQuizzes = Quizzes.stream().map( quiz -> {

            quiz.setQuestions(questionClient.getQuestionOfQuiz(quiz.getId()));
            return quiz;

        }).collect(Collectors.toList());
        return newQuizzes;
    }

    @Override
    public Quiz get(Long id) {

        Quiz quiz = quizRepo.findById(id).orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setQuestions(questionClient.getQuestionOfQuiz(quiz.getId()));


        return quiz;
    }
}
