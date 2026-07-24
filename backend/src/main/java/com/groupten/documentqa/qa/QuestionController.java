package com.groupten.documentqa.qa;

import com.groupten.documentqa.qa.dto.QuestionRequest;
import com.groupten.documentqa.qa.dto.QuestionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public QuestionResponse ask(@RequestBody QuestionRequest request) {
        return questionService.answer(request.question());
    }
}
