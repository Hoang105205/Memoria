package com.example.memoria.data.model.dto;

import java.util.List;

/**
 * A Data Transfer Object (DTO) representing the top-level response from the Dictionary API.
 * Contains the queried word along with its phonetics and meanings.
 */
public class DictionaryResponse {

    /**
     * The vocabulary word that was queried.
     */
    public String word;

    /**
     * A list of meanings/definitions grouped by part of speech.
     */
    public List<Meaning> meanings;

    /**
     * A list of phonetic transcriptions and audio pronunciations.
     */
    public List<Phonetic> phonetics;
}
