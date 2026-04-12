package com.example.memoria.data.model.dto;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * A Data Transfer Object (DTO) representing a deck that has been published to the community.
 * Used for fetching, parsing, and displaying shared decks from the public Firestore collection.
 * * Note: @IgnoreExtraProperties ensures that if Firestore documents have additional
 * fields added in the future (not defined here), the app won't crash during deserialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgnoreExtraProperties
public class PublicDeck {

    // ========================================================================
    // Identification Information
    // ========================================================================

    /**
     * The unique document ID of this public deck in the global Firestore collection.
     */
    private String publicDocId;

    /**
     * The original ID of the deck from the author's local database.
     */
    private String originalDeckId;

    /**
     * The Firebase User ID (UID) of the person who published the deck.
     */
    private String authorId;

    /**
     * The display name of the author.
     */
    private String authorName;

    // ========================================================================
    // Deck Details
    // ========================================================================

    /**
     * The name or title of the published deck.
     */
    private String deckName;

    /**
     * The hex color code for the deck's cover.
     */
    private String coverColor;

    /**
     * A list of keyword sub-strings generated for this deck to facilitate
     * fast searching on Firestore.
     */
    private List<String> searchKeywords;

    // ========================================================================
    // Statistics & Date
    // ========================================================================

    /**
     * The total number of times this deck has been downloaded/cloned by other users.
     */
    private long downloadCount;

    /**
     * The total number of flashcards contained within this deck.
     */
    private long totalCards;

    /**
     * Timestamp indicating exactly when the deck was published to the community.
     */
    private Date publishedAt;
}