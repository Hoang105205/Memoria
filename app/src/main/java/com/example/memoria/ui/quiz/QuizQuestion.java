package com.example.memoria.ui.quiz;

import com.example.memoria.data.model.entity.Card;

import java.util.List;
import java.util.UUID;

public class QuizQuestion {
    public enum Type { WORD, AUDIO, SYNONYM }

    public UUID id;
    public String word;
    public String meaning;
    public Type questionType;
    public List<String> options;
    public String correctAnswer;

    public QuizQuestion(UUID id, String word, String meaning, Type questionType) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.questionType = questionType;
    }
}