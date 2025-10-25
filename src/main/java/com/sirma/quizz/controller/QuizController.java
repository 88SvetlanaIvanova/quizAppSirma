package com.sirma.quizz.controller;

import com.sirma.quizz.model.Question;
import com.sirma.quizz.model.QuizSubmission;
import com.sirma.quizz.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@SessionAttributes("currentQuiz")
public class QuizController {
    @Autowired
    private QuizService quizService;
    private List<Question> currentQuiz;

    @ModelAttribute("currentQuiz")
    public List<Question> getQuiz() {
        return quizService.getQuiz();
    }
    @GetMapping("/quiz")
    public String showQuiz(Model model) {
        model.addAttribute("questions", model.getAttribute("currentQuiz"));
        return "quiz";
    }

    @PostMapping("/submit")
    public String submitQuiz(@ModelAttribute QuizSubmission submission,
                             @ModelAttribute("currentQuiz") List<Question> currentQuiz,
                             Model model) {
        List<Integer> answers = submission.getAnswers();
        int score = 0;
        List<Boolean> quizResults = new ArrayList<>();

        for (int i = 0; i < currentQuiz.size() ; i++) {
            int userAnswer = answers.get(i);
            boolean isCorrect = userAnswer != -1
                    && currentQuiz.get(i).getCorrectIndex() == userAnswer;
            quizResults.add(isCorrect);
            if (isCorrect) score++;
        }
        model.addAttribute("score", score);
        model.addAttribute("questions", currentQuiz);
        model.addAttribute("answers", answers);
        model.addAttribute("quizResults", quizResults);
        System.out.println("Answers: " + answers);
        System.out.println("QuizResults: " + quizResults);

        return "result";
    }
}
