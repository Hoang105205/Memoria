package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a flashcard in the Memoria application.
 * Contains basic information, Spaced Repetition System (SRS) metrics, date tracking, and Firestore synchronization details.
 * Using CASCADE for auto delete card when delete deck.
 */
@Entity(
        tableName = "cards",
        foreignKeys = @ForeignKey(
                entity = Deck.class,
                parentColumns = "deck_id",
                childColumns = "deck_id",
                onDelete = ForeignKey.CASCADE
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card implements Serializable {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the card.
     * * Note on Firestore @Exclude:
     * 1. Prevents Data Crash: Firestore does not natively support the UUID data type
     * and will throw an exception during automatic serialization/deserialization.
     * 2. Avoids Redundancy: This ID is already utilized as the Document ID in Firestore.
     * * ID mapping is handled manually via SyncRepository.
     */
    @PrimaryKey
    @ColumnInfo(name = "card_id")
    @androidx.annotation.NonNull
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID cardId;

    /**
     * Foreign key referencing the parent Deck.
     * Indexed for faster database queries.
     * * Note on Firestore @Exclude:
     * Excluded to prevent Firestore from crashing when attempting to parse the UUID type.
     * The deck reference is manually extracted from the document path during the pull sync.
     */
    @ColumnInfo(name = "deck_id", index = true)
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID deckId;

    /**
     * Type of the card.
     * Values: 0 (Text), 1 (Image), 2 (Audio).
     */
    @ColumnInfo(name = "card_type")
    private int cardType;

    /**
     * The primary text displayed on the front of the flashcard.
     */
    @ColumnInfo(name = "front_text")
    private String frontText;

    /**
     * URI to Cloudinary storage for the image displayed on the front of the card.
     */
    @ColumnInfo(name = "front_image")
    private String frontImage;

    /**
     * List of part of speech tags for the back of the card.
     */
    @ColumnInfo(name = "back_types")
    private List<String> backTypes;

    /**
     * List of definitions or meanings corresponding to the back of the card.
     */
    @ColumnInfo(name = "back_meanings")
    private List<String> backMeanings;

    // ========================================================================
    // Ease Factor & Review Data
    // ========================================================================

    /**
     * The ease factor used in the Spaced Repetition System (SRS) algorithm.
     * Determines how quickly the review interval grows.
     */
    @ColumnInfo(name = "ease_factor")
    private double easeFactor;

    /**
     * The number of days until the card needs to be reviewed next.
     */
    @ColumnInfo(name = "interval_days")
    private int intervalDays;

    /**
     * The score or result of the user's most recent review.
     */
    @ColumnInfo(name = "last_result")
    private float lastResult;

    /**
     * The total number of times this card has been reviewed.
     */
    @ColumnInfo(name = "review_count")
    private int reviewCount;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating when the card was created in the local database.
     */
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    /**
     * Timestamp indicating the last time the card's data was updated.
     */
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    /**
     * Timestamp indicating the exact date and time of the user's last review.
     */
    @ColumnInfo(name = "last_review_at")
    private Date lastReviewAt;

    /**
     * The scheduled date for the next review based on the SRS algorithm.
     */
    @ColumnInfo(name = "next_review_date")
    private Date nextReviewDate;

    // ========================================================================
    // Firestore & Sync Status
    // ========================================================================

    /**
     * The corresponding Document ID in Firebase Firestore.
     */
    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    /**
     * Synchronization status of the card with the remote server.
     * 0 for un-synced, 1 for synced, 2 for deleted.
     */
    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}