package com.example.memoria.data.model.dto;
import java.util.List;

public class CardAudioItem {
    public final String frontText;
    public final List<String> backMeanings;

    public CardAudioItem(String frontText, List<String> backMeanings) {
        this.frontText = frontText;
        this.backMeanings = backMeanings;
    }
}
