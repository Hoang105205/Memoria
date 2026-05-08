package com.example.memoria.data.model.dto;

import java.util.List;

/**
 * A Data Transfer Object representing a specific meaning of a word, categorized by its part of speech.
 */
public class Meaning {

    /**
     * The part of speech (e.g., noun, verb, adjective).
     */
    public String partOfSpeech;

    /**
     * A list of detailed definitions under this part of speech.
     */
    public List<Definition> definitions;
    public List<String> synonyms;
    public List<String> antonyms;
}