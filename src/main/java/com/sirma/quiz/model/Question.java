package com.sirma.quiz.model;

import lombok.*;

import java.util.Collections;
import java.util.List;
@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    private String text;
    private List<String> options;
    private int correctIndex;

    public void shuffleOptions() {
        if (options == null || options.isEmpty()) return;

        String correctAnswer = options.get(correctIndex);
        Collections.shuffle(options);
        this.correctIndex = options.indexOf(correctAnswer);
    }
}
