package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a favorite word in the Memoria application.
 * Contains word details, foreign key to its parent folder, date tracking, and Firestore synchronization details.
 * Using CASCADE for auto delete word when delete folder.
 */
@Entity(
        tableName = "fav_words",
        foreignKeys = @ForeignKey(
                entity = FavFolder.class,
                parentColumns = "folder_id",
                childColumns = "folder_id",
                onDelete = ForeignKey.CASCADE
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavWord {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the favorite word.
     * * Note on Firestore @Exclude:
     * 1. Prevents Data Crash: Firestore does not natively support the UUID data type
     * and will throw an exception during automatic serialization/deserialization.
     * 2. Avoids Redundancy: This ID is already utilized as the Document ID in Firestore.
     * * ID mapping is handled manually via SyncRepository.
     */
    @PrimaryKey
    @ColumnInfo(name = "fav_id")
    @androidx.annotation.NonNull
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID favId;

    /**
     * Foreign key referencing the parent FavFolder.
     * Indexed for faster database queries.
     * * Note on Firestore @Exclude:
     * Excluded to prevent Firestore from crashing when attempting to parse the UUID type.
     * The folder reference is manually extracted from the document path during the pull sync.
     */
    @ColumnInfo(name = "folder_id", index = true)
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID folderId;

    /**
     * The vocabulary word text.
     */
    @ColumnInfo(name = "word_text")
    private String wordText;

    /**
     * The part of speech of the word (e.g., noun, verb, adjective).
     */
    @ColumnInfo(name = "part_of_speech")
    private String partOfSpeech;

    /**
     * A brief definition or short meaning of the word.
     */
    @ColumnInfo(name = "short_meaning")
    private String shortMeaning;

    /**
     * Indicates whether the word is pinned to the top of the list.
     */
    @ColumnInfo(name = "pin_status")
    private boolean pinStatus;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating when the word was added to favorites.
     */
    @ColumnInfo(name = "added_at")
    private Date addedAt;

    /**
     * Timestamp indicating the last time the word's data was updated.
     */
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    // ========================================================================
    // Firestore & Sync Status
    // ========================================================================

    /**
     * The corresponding Document ID in Firebase Firestore.
     */
    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    /**
     * Synchronization status of the word with the remote server.
     * 0 for un-synced, 1 for synced, 2 for deleted.
     */
    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}
