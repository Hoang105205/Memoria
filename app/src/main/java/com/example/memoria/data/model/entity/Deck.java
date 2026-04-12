package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity class representing a flashcard deck in the Memoria application.
 * Contains basic information, date tracking, sharing details, and Firestore synchronization status.
 */
@Entity(tableName = "decks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the deck.
     * * Note on Firestore @Exclude:
     * 1. Prevents Data Crash: Firestore does not natively support the UUID data type
     * and will throw an exception during automatic serialization/deserialization.
     * 2. Avoids Redundancy: This ID is already utilized as the Document ID in Firestore.
     * * ID mapping is handled manually via SyncRepository.
     */
    @PrimaryKey
    @ColumnInfo(name = "deck_id")
    @androidx.annotation.NonNull
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID deckId;

    /**
     * The name of the flashcard deck.
     */
    @ColumnInfo(name = "deck_name")
    private String deckName;

    /**
     * The hex color code for the cards cover in the deck.
     */
    @ColumnInfo(name = "cover_color")
    private String coverColor;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating when the deck was created in the local database.
     */
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    /**
     * Timestamp indicating the last time the deck's data was updated.
     */
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    // ========================================================================
    // Sharing Information
    // ========================================================================

    /**
     * A unique short code used to share this deck with other users.
     */
    @ColumnInfo(name = "share_code")
    private String shareCode;

    /**
     * Timestamp indicating when the deck was shared with other users.
     */
    @ColumnInfo(name = "shared_at")
    private Date sharedAt;

    // ========================================================================
    // Firestore & Sync Status
    // =======================================================================

    /**
     * The corresponding Document ID in Firebase Firestore.
     */
    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    /**
     * Synchronization status of the deck with the remote server.
     * 0 for un-synced, 1 for synced, 2 for deleted.
     */
    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}