package com.sirma.quizz.controller;

import com.sirma.quizz.QuizSession;
import com.sirma.quizz.model.Question;
import com.sirma.quizz.model.QuizInfo;
import com.sirma.quizz.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Controller
public class QuizController {

    private final QuizService quizService;
    private final QuizSession quizSession;

    public QuizController(QuizService quizService, QuizSession quizSession) {
        this.quizService = quizService;
        this.quizSession = quizSession;
    }


    @GetMapping("/quizzes")
    public String showQuizList(Model model) {
        List<QuizInfo> quizzes = quizService.getAvailableQuizzes();
        model.addAttribute("quizzes", quizzes);
        return "quizzes";
    }

    @GetMapping("/quiz/load/{topic}")
    @ResponseBody
    public List<Question> getQuiz(@PathVariable String topic) {
        System.out.println("Requested quiz topic: " + topic);
        return quizService.getQuiz(topic);
    }


    @GetMapping("/quiz/{topic}/{index}")
    public String showQuestion(@PathVariable String topic, @PathVariable int index, Model model) {

        if (!topic.equals(quizSession.getTopic()) || !quizSession.isValid()) {
            var quiz = quizService.getQuiz(topic);
            if (quiz == null || quiz.isEmpty()) {
                model.addAttribute("error", "Quiz not found or empty.");
                return "error";
            }
            quizSession.startNew(quiz, topic);
        }

        var quiz = quizSession.getQuiz();
        var answers = quizSession.getAnswers();

        if (index < 0 || index >= quiz.size()) {
            model.addAttribute("error", "Invalid question index.");
            return "error";
        }

//        @SuppressWarnings("unchecked")
//        List<Question> quiz = (List<Question>) session.getAttribute("currentQuiz");
//
//        String sessionTopic = (String) session.getAttribute("topic");
//        if (quiz == null || sessionTopic == null || !Objects.equals(sessionTopic, topic)) {
//            quiz = quizService.getQuiz(topic);
//            session.setAttribute("currentQuiz", quiz);
//            session.setAttribute("topic", topic);
//            model.addAttribute("error", "Quiz not found or empty.");
//            List<Integer> answers = new ArrayList<>(Collections.nCopies(quiz.size(), -1));
//            session.setAttribute("answers", answers);
//        }
//
//        if (quiz.isEmpty()) {
//            model.addAttribute("error", "Quiz not found or empty.");
//            return "error";
//        }
//
//        @SuppressWarnings("unchecked")
//        List<Integer> answers = (List<Integer>) session.getAttribute("answers");
//        if (answers == null || answers.size() != quiz.size()) {
//            answers = new ArrayList<>(Collections.nCopies(quiz.size(), -1));
//            session.setAttribute("answers", answers);
//        }
//
//        if (index < 0 || index >= quiz.size()) {
//            model.addAttribute("error", "Invalid question index.");
//            return "error";
//        }

        model.addAttribute("question", quiz.get(index));
        model.addAttribute("index", index);
        model.addAttribute("total", quiz.size());
        model.addAttribute("selectedAnswer", answers.get(index));
        model.addAttribute("topic", topic);

        return "question";
    }

    @PostMapping("/quiz/{index}")
    public String saveAnswer(@PathVariable int index,
                             @RequestParam("answer") int answer,
                             @RequestParam("action") String action,
                             @RequestParam("topic") String topic) {

//        @SuppressWarnings("unchecked")
//        List<Integer> answers = (List<Integer>) session.getAttribute("answers");
//        //answers.set(index, answer);
//        if (answers == null) {
//            @SuppressWarnings("unchecked")
//            List<Question> quiz = (List<Question>) session.getAttribute("currentQuiz");
//            if (quiz == null) {
//                return "redirect:/quizzes";
//            }
//            answers = new ArrayList<>(Collections.nCopies(quiz.size(), -1));
//            session.setAttribute("answers", answers);
//        }
//
//        if (index >= 0 && index < answers.size()) {
//            answers.set(index, answer);
//        }
//
//        if (topic.contains(",")) {
//            topic = topic.split(",")[0].trim();
//        }
        if (!quizSession.isValid() || !topic.equals(quizSession.getTopic())) {
            return "redirect:/quizzes";
        }

        quizSession.setAnswer(index, answer);

        return switch (action) {
            case "next" -> "redirect:/quiz/" + topic + "/" + (index + 1);
            case "prev" -> "redirect:/quiz/" + topic + "/" + (index - 1);
            default -> "redirect:/submit";
        };
    }

    @GetMapping("/submit")
    public String submitQuiz(Model model) {

        if (!quizSession.isValid()) {
            model.addAttribute("error", "Your quiz session has expired or is invalid. Please start again.");
            return "error";
        }

        List<Question> quiz = quizSession.getQuiz();
        List<Integer> answers = quizSession.getAnswers();
//        @SuppressWarnings("unchecked")
//        List<Question> quiz = (List<Question>) session.getAttribute("currentQuiz");
//        @SuppressWarnings("unchecked")
//        List<Integer> answers = (List<Integer>) session.getAttribute("answers");

//        if (quiz == null || answers == null) {
//            // session expired or user navigated incorrectly — send back to list with an explanation
//            model.addAttribute("error", "Your quiz session has expired or is invalid. Please start again.");
//            return "error";
//        }

        int score = 0;
        List<Boolean> quizResults = new ArrayList<>();

        for (int i = 0; i < quiz.size() ; i++) {
            int userAnswer = answers.get(i);
            boolean isCorrect = userAnswer != -1
                    && quiz.get(i).getCorrectIndex() == userAnswer;
            quizResults.add(isCorrect);
            if (isCorrect) score++;
        }

        model.addAttribute("topic", quizSession.getTopic());
        model.addAttribute("score", score);
        model.addAttribute("questions", quiz);
        model.addAttribute("answers", answers);
        model.addAttribute("quizResults", quizResults);
        System.out.println("Answers: " + answers);
        System.out.println("QuizResults: " + quizResults);
        quizSession.clear();

        return "result";
    }



}
