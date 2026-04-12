package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

/**
 * Entity class representing a user's local search history in the Memoria application.
 * Used to keep track of recently searched words for quick suggestions.
 * * Note: This is a local-only entity and is not synchronized with Firestore.
 * Therefore, it does not require @Exclude annotations or sync tracking fields
 * (like sync_status or firestore_id).
 */
@Entity(
        tableName = "search_histories",
        indices = {@Index(value = {"word_text"}, unique = true)}
)
@Data
public class SearchHistory {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the search history record.
     */
    @PrimaryKey
    @ColumnInfo(name = "history_id")
    @androidx.annotation.NonNull
    private UUID historyId;

    /**
     * The vocabulary word that the user searched for.
     * Indexed and constrained to be unique to prevent duplicate history entries
     * for the exact same word.
     */
    @ColumnInfo(name = "word_text")
    private String wordText;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating when the user last searched for this word.
     * Can be used to sort the search history from newest to oldest.
     */
    @ColumnInfo(name = "searched_at")
    private Date searchedAt;
}