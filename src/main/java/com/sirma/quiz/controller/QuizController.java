package com.sirma.quiz.controller;

import com.sirma.quiz.QuizSession;
import com.sirma.quiz.model.Question;
import com.sirma.quiz.model.QuizInfo;
import com.sirma.quiz.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class QuizController {

    private record QuizEvaluation(int score, List<Boolean> quizResults) {}
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
    public String showQuestion(@PathVariable String topic, @PathVariable int index, Model model,
                               HttpSession session) {

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
        if (session.getAttribute("quizStartTime") == null) {
            session.setAttribute("quizStartTime", System.currentTimeMillis());
            session.setAttribute("quizDurationMillis", 10L * 60L * 1000L); // 10 min
        }

        Number startObj = (Number) session.getAttribute("quizStartTime");
        Number durationObj = (Number) session.getAttribute("quizDurationMillis");

        long start = startObj.longValue();
        long duration = durationObj.longValue();
        long remaining = Math.max(0, (start + duration) - System.currentTimeMillis());
        model.addAttribute("timeRemaining", remaining / 1000); // seconds

        model.addAttribute("question", quiz.get(index));
        model.addAttribute("index", index);
        model.addAttribute("total", quiz.size());
        model.addAttribute("selectedAnswer", answers.get(index));
        model.addAttribute("topic", topic);

        return "question";
    }

    @PostMapping("/quiz/{index}")
    public String saveAnswer(@PathVariable int index,
                             @RequestParam(required = false) Integer answer,
                             @RequestParam(required = false) String action,
                             @RequestParam("topic") String topic) {

        if (action == null) {
            return "redirect:/result?timeout=true";
        }
        if (!quizSession.isValid() || !topic.equals(quizSession.getTopic())) {
            return "redirect:/quizzes";
        }
        return switch (action) {
            case "next" -> "redirect:/quiz/" + topic + "/" + (index + 1);
            case "prev" -> "redirect:/quiz/" + topic + "/" + (index - 1);
            default -> "redirect:/submit";
        };
    }

    @GetMapping("/submit")
    public String submitQuiz(HttpSession session, Model model,
                             @RequestParam(required = false) Boolean timeout) {

        long start = (long) session.getAttribute("quizStartTime");
        long duration = (long) session.getAttribute("quizDurationMillis");
        boolean timedOut = System.currentTimeMillis() > start + duration || Boolean.TRUE.equals(timeout);

        if (timedOut) {
            model.addAttribute("timeoutMessage", "⏰ Time’s up! Your quiz was automatically submitted.");
        }

        if (!quizSession.isValid()) {
            model.addAttribute("error", "Your quiz session has expired or is invalid. Please start again.");
            return "error";
        }

        List<Question> quiz = quizSession.getQuiz();
        List<Integer> answers = quizSession.getAnswers();

        QuizEvaluation evaluation = evaluateQuiz(quiz, answers);

        model.addAttribute("topic", quizSession.getTopic());
        model.addAttribute("score", evaluation.score);
        model.addAttribute("questions", quiz);
        model.addAttribute("answers", answers);
        model.addAttribute("quizResults", evaluation.quizResults);

        if (Boolean.TRUE.equals(timeout)) {
            model.addAttribute("timeoutMessage", "⏰ Time’s up! Your quiz was automatically submitted.");
        }
        int percentage = (!quiz.isEmpty()) ? (evaluation.score() * 100 / quiz.size()) : 0;
        model.addAttribute("percentage", percentage);

        return "result";
    }

    @GetMapping("/result")
    public String showResult(@RequestParam(required = false) Boolean timeout, Model model) {
        if (!quizSession.isValid()) {
            return "redirect:/quizzes";
        }

        List<Question> quiz = quizSession.getQuiz();
        List<Integer> answers = quizSession.getAnswers();

        QuizEvaluation evaluation = evaluateQuiz(quiz, answers);

        model.addAttribute("topic", quizSession.getTopic());
        model.addAttribute("questions", quiz);
        model.addAttribute("answers", answers);
        model.addAttribute("score", evaluation.score());
        model.addAttribute("quizResults", evaluation.quizResults());

        if (Boolean.TRUE.equals(timeout)) {
            model.addAttribute("timeoutMessage", "⏰ Time’s up! Your quiz was automatically submitted.");
        }
        int percentage = (!quiz.isEmpty()) ? (evaluation.score() * 100 / quiz.size()) : 0;
        model.addAttribute("percentage", percentage);
        return "result";
    }

    private static QuizEvaluation evaluateQuiz(List<Question> quiz, List<Integer> answers) {
        int score = 0;
        List<Boolean> quizResults = new ArrayList<>();

        for (int i = 0; i < quiz.size(); i++) {
            int userAnswer = answers.get(i);
            boolean isCorrect = userAnswer != -1 && quiz.get(i).getCorrectIndex() == userAnswer;
            quizResults.add(isCorrect);
            if (isCorrect) score++;
        }
        return new QuizEvaluation(score, quizResults);
    }
    
}
