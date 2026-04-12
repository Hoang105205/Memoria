package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Entity class representing a user's quiz history/result for a specific deck.
 * Contains score details, time taken, and Firestore synchronization details.
 * Using CASCADE for auto delete history when the parent deck is deleted.
 */
@Entity(
        tableName = "quiz_his",
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
public class QuizHistory implements Serializable {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the quiz result.
     * * Note on Firestore @Exclude:
     * 1. Prevents Data Crash: Firestore does not natively support the UUID data type.
     * 2. Avoids Redundancy: This ID is already utilized as the Document ID in Firestore.
     */
    @PrimaryKey
    @ColumnInfo(name = "result_id")
    @androidx.annotation.NonNull
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID resultId;

    /**
     * Foreign key referencing the Deck that this quiz belongs to.
     * Indexed for faster database queries.
     * * Note on Firestore @Exclude:
     * Excluded to prevent crash on UUID type. It is mapped manually using custom
     * getter/setter with @PropertyName for Firestore string serialization.
     */
    @ColumnInfo(name = "deck_id", index = true)
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID deckId;

    // ========================================================================
    // Quiz Results
    // ========================================================================

    /**
     * The number of correctly answered questions in this quiz.
     */
    @ColumnInfo(name = "correct_count")
    private int correctCount;

    /**
     * The total number of questions in this quiz.
     */
    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    /**
     * The time taken to complete the quiz, usually in seconds.
     */
    @ColumnInfo(name = "time_taken")
    private int timeTaken;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating when the quiz was taken.
     */
    @ColumnInfo(name = "taken_at")
    private Date takenAt;

    /**
     * Timestamp indicating when this history record expires (e.g., for auto-cleanup).
     */
    @ColumnInfo(name = "expire_at")
    private Date expireAt;

    // ========================================================================
    // Firestore & Sync Status
    // ========================================================================

    /**
     * The corresponding Document ID in Firebase Firestore.
     */
    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    /**
     * Synchronization status of the history record with the remote server.
     * 0 for un-synced, 1 for synced, 2 for deleted.
     */
    @ColumnInfo(name = "sync_status")
    private int syncStatus;

    // ========================================================================
    // Firestore Mapping Methods
    // ========================================================================

    /**
     * Custom getter to expose the deck_id as a String to Firestore.
     * This bypasses the UUID limitation while keeping the database relation intact.
     * * @return The UUID string representation of the deckId.
     */
    @PropertyName("deck_id_string")
    public String getFirestoreDeckId() {
        return deckId != null ? deckId.toString() : null;
    }

    /**
     * Custom setter to receive the deck_id as a String from Firestore.
     * * @param idStr The UUID string representation from Firestore.
     */
    @PropertyName("deck_id_string")
    public void setFirestoreDeckId(String idStr) {
        this.deckId = idStr != null ? UUID.fromString(idStr) : null;
    }
}