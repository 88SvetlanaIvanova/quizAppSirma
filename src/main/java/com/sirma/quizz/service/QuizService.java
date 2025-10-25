package com.sirma.quizz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.quizz.model.Question;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuizService {
    private List<Question> questions;

    @PostConstruct
    public void init() {
        questions = loadQuestionsFromFile();
        Collections.shuffle(questions);
    }

    private List<Question> loadQuestionsFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/questions.json")) {
            return mapper.readValue(is, new TypeReference<List<Question>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Question> getQuiz() {
        return questions.subList(0, 5);
    }

//    public int evaluate(List<Integer> userAnswers) {
//        int score = 0;
//        for (int i = 0; i < userAnswers.size() ; i++) {
//            if (questions.get(i).getCorrectIndex() == userAnswers.get(i)) {
//                score++;
//            }
//        }
//        return score;
//    }
}
