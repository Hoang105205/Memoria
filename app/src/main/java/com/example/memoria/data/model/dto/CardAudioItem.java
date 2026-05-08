package com.example.memoria.data.model.dto;
import java.util.List;

/**
 * A Data Transfer Object (DTO) used specifically for the audio playback feature.
 * It encapsulates only the necessary text components of a card (front text and back meanings)
 * without carrying the heavy overhead of the full Card entity.
 */
public class CardAudioItem {

    // ========================================================================
    // Audio Content
    // ========================================================================

    /**
     * The primary text to be read aloud (typically the vocabulary word).
     */
    public final String frontText;

    /**
     * The list of meanings associated with the word, used for context or secondary audio.
     */
    public final List<String> backMeanings;

    // ========================================================================
    // Constructor
    // ========================================================================

    /**
     * Constructs a new CardAudioItem.
     *
     * @param frontText    The text to be read aloud.
     * @param backMeanings The list of meanings.
     */
    public CardAudioItem(String frontText, List<String> backMeanings) {
        this.frontText = frontText;
        this.backMeanings = backMeanings;
    }
}
