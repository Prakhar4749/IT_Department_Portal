package com.result.services.impl;


import com.result.entities.Product;
import com.result.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository quizRepo;

    @Autowired
    private QuestionClient questionClient;



    @Override
    public Product add(Product quiz) {
        return quizRepo.save(quiz);
    }

    @Override
    public List<Product> get() {

        List<Product> Productzes = quizRepo.findAll();

        List<Product> newProductzes = Productzes.stream().map( quiz -> {

            quiz.setQuestions(questionClient.getQuestionOfProduct(quiz.getId()));
            return quiz;

        }).collect(Collectors.toList());
        return newProductzes;
    }

    @Override
    public Product get(Long id) {

        Product quiz = quizRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        quiz.setQuestions(questionClient.getQuestionOfProduct(quiz.getId()));


        return quiz;
    }
}
