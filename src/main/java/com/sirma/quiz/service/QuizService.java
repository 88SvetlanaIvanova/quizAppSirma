package com.sirma.quiz.service;

import com.sirma.quiz.model.QuizInfo;
import com.sirma.quiz.model.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);
    private final List<QuizLoader> loaders;
    private static final int MAX_QUESTIONS_PER_QUIZ = 5;

    public QuizService(List<QuizLoader> loaders) {
        this.loaders = loaders;
    }

    public List<Question> getQuiz(String topic) {

        if (topic == null || topic.isBlank()) return Collections.emptyList();
        topic = topic.trim();

        List<String> candidates = new ArrayList<>();
        if (topic.endsWith(".json") || topic.endsWith(".csv")) {
            candidates.add(topic);
        } else {
            candidates.add(topic + ".json");
            candidates.add(topic + ".csv");
        }

        List<Question> loaded = Collections.emptyList();
        for (String fileName : candidates) {
            if (fileName == null) continue;
            for (QuizLoader loader : loaders) {
                try {
                    if (loader.isSupported(fileName)) {
                        var result = loader.load(fileName);
                        if (result != null && !result.isEmpty()) {
                            loaded = result;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    log.error("Failed to load quiz: {}", fileName ,ex);
                }
            }
            if (!loaded.isEmpty()) break;
        }

        if (loaded.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(loaded);

        for (Question q : loaded) {
            if (q.getOptions() != null) {
                q.shuffleOptions();
            }
        }

        int limit = Math.min(MAX_QUESTIONS_PER_QUIZ, loaded.size());
        return new ArrayList<>(loaded.subList(0, limit));
    }

    public List<QuizInfo> getAvailableQuizzes() {
        List<QuizInfo> quizInfos = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources =resolver.getResources("classpath*:quizzes/*.{json,csv}");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    quizInfos.add(new QuizInfo(filename.toLowerCase(Locale.ROOT).trim()));
                }
            }
        } catch (IOException e) {
            log.error("Failed to load available quizzes.", e);
        }
        quizInfos.sort(Comparator.comparing(QuizInfo::getDisplayName));
        return quizInfos;
    }

}
