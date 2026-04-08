package com.example.memoria.data.model.dto;

import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgnoreExtraProperties
public class PublicDeck {
    private String publicDocId;
    private String originalDeckId;
    private String authorId;
    private String authorName;
    private String deckName;
    private String coverColor;
    private List<String> searchKeywords;
    private long downloadCount;
    private long totalCards;
    private Date publishedAt;
}