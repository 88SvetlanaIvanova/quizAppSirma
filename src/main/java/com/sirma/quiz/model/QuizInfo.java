package com.sirma.quiz.model;
import lombok.Getter;
import java.io.IOException;

@Getter
public class QuizInfo {

    private final String fileName;
    private final String displayName;

    public QuizInfo(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("QuizInfo fileName cannot be null or blank");
        }
        this.fileName = fileName;
        this.displayName = formatDisplayName(fileName);
    }

    private String formatDisplayName(String fileName) throws IOException {
        String base = fileName
                .replaceAll("\\.json$", "")
                .replaceAll("\\.csv$", "")
                .replace('_', ' ')
                .trim();

        if (base.isEmpty()) return fileName;
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }

}
