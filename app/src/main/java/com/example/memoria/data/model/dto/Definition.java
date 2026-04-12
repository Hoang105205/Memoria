package com.example.memoria.data.model.dto;

import java.util.List;

/**
 * A Data Transfer Object representing a single dictionary definition of a word.
 */
public class Definition {

    /**
     * The text describing the meaning of the word.
     */
    public String definition;

    /**
     * An example sentence using the word in context.
     */
    public String example;

    /**
     * A list of synonymous words with similar meanings.
     */
    public List<String> synonyms;
}

