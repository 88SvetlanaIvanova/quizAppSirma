package com.sirma.quiz.service;
import com.sirma.quiz.model.Question;
import java.util.List;
public interface QuizLoader {
    boolean isSupported(String fileName);

    List<Question> load(String fileName);
}
