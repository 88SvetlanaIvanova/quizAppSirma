package com.sirma.quizz.service;

import com.sirma.quizz.model.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.quizz.model.Question;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JsonQuizLoader implements QuizLoader{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isSupported(String fileName) {
        return fileName.toLowerCase().endsWith(".json");
    }

    @Override
    public List<Question> load(String fileName) {
        try {
            var resource = new ClassPathResource("quizzes/" + fileName);
            if (!resource.exists()) return Collections.emptyList();

            return mapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
