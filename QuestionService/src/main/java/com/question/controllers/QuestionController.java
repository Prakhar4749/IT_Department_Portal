package com.question.controllers;

import com.question.entities.Question;
import com.question.services.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController {
    private QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public Question create (@RequestBody Question question){
        return questionService.add(question);

    }

    @GetMapping
    public List<Question> get() {
        return questionService.get();
    }

    @GetMapping("/{id}")
    public Question getOne(@PathVariable Long id) {
        return questionService.get(id);
    }

    @GetMapping("/quiz/{quizId}")
    public List<Question> getByQuizId(@PathVariable Long quizId) {
        return questionService.getQuestionOfQuizId(quizId);
    }
}
