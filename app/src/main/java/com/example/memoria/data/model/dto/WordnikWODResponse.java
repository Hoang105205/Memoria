package com.example.memoria.data.model.dto;

import java.util.List;

public class WordnikWODResponse {
    public String word;
    public String note;
    public List<WordnikDefinition> definitions;
    public List<WordnikExample> examples;

    public static class WordnikDefinition {
        public String text;
        public String partOfSpeech;
    }

    public static class WordnikExample {
        public String text;
    }
}
