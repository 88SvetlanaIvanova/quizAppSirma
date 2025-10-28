package com.sirma.quiz.service;
import com.sirma.quiz.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
public class CsvQuizLoader implements QuizLoader{

    private static final Logger log = LoggerFactory.getLogger(CsvQuizLoader.class);

    @Override
    public boolean isSupported(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    @Override
    public List<Question> load(String fileName) {
        var resource = new ClassPathResource("quizzes/" + fileName);
        if (!resource.exists()) return Collections.emptyList();

        List<Question> questions = new ArrayList<>();

        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            var format = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();
            var parser = format.parse(reader);

            for (CSVRecord record : parser) {
                String questionText = record.get("question").trim();
                List<String> options = new ArrayList<>();
                try {
                    for (int i = 1; i <= 4; i++) {
                        String opt = record.get("option" + i);
                        if (opt != null && !opt.isBlank()) options.add(opt.trim());
                    }
                    int correct = Integer.parseInt(record.get("correctIndex").trim());
                    if (correct > 0) correct -= 1;

                    Question q = new Question();
                    q.setText(questionText);
                    q.setOptions(options);
                    q.setCorrectIndex(correct);

                    questions.add(q);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (Exception e) {
            log.error("Failed to load quiz file: {}", fileName, e);
            return Collections.emptyList();
        }
        return questions.size() > 5 ? questions.subList(0, 5) : questions;
    }
}
