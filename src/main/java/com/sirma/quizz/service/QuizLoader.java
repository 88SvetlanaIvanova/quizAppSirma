package com.sirma.quizz.service;
import com.sirma.quizz.model.Question;
import java.util.List;
public interface QuizLoader {
    boolean isSupported(String fileName);

    List<Question> load(String fileName);
}
