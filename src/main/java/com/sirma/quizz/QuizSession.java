package com.sirma.quizz;
import com.sirma.quizz.model.Question;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
public class QuizSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Question> quiz;
    private List<Integer> answers;
    private String topic;

    public void startNew(List<Question> quiz, String topic) {
        this.quiz = (quiz == null) ? null : new ArrayList<>(quiz);
        this.topic = topic;
        if (this.quiz != null) {
            this.answers = new ArrayList<>();
            for (int i = 0; i < this.quiz.size(); i++) this.answers.add(-1);
        } else {
            this.answers = null;
        }
    }

    public List<Question> getQuiz() { return quiz; }
    public List<Integer> getAnswers() { return answers; }
    public String getTopic() { return topic; }

    public void setAnswer(int index, int answer) {
        if (answers != null && index >= 0 && index < answers.size()) {
            answers.set(index, answer);
        }
    }

    public void clear() {
        this.quiz = null;
        this.answers = null;
        this.topic = null;
    }

    public boolean isValid() {
        return quiz != null && answers != null && Objects.equals(quiz.size(), answers.size());
    }
}
