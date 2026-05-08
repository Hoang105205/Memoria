package com.example.memoria.data.model.dto;

/**
 * A Data Transfer Object representing the phonetic transcription and pronunciation audio of a word.
 */
public class Phonetic {

    /**
     * The phonetic spelling (e.g., IPA notation like /kæt/).
     */
    public String text;

    /**
     * A URL link to an audio file pronouncing the word.
     */
    public String audio;
}
