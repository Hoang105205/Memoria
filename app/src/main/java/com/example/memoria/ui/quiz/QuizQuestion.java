package com.example.memoria.ui.quiz;

import com.example.memoria.data.model.Card;

import java.util.List;

public class QuizQuestion {
    public enum Type { AUDIO_TO_WORD, WORD_TO_MEANING }

    public Type questionType;
    public Card correctCard;
    public List<String> options;
    public String correctAnswer;

    public QuizQuestion(Type questionType, Card correctCard, List<String> options, String correctAnswer) {
        this.questionType = questionType;
        this.correctCard = correctCard;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}