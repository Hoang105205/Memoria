package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a folder for favorite words in the Memoria application.
 * Contains basic folder information, date tracking, and Firestore synchronization details.
 */
@Entity(tableName = "fav_folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavFolder {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the favorite folder.
     * * Note on Firestore @Exclude:
     * 1. Prevents Data Crash: Firestore does not natively support the UUID data type
     * and will throw an exception during automatic serialization/deserialization.
     * 2. Avoids Redundancy: This ID is already utilized as the Document ID in Firestore.
     * * ID mapping is handled manually via SyncRepository.
     */
    @PrimaryKey
    @ColumnInfo(name = "folder_id")
    @androidx.annotation.NonNull
    @Exclude
    @lombok.Getter(onMethod_ = {@Exclude})
    @lombok.Setter(onMethod_ = {@Exclude})
    private UUID folderId;

    /**
     * The name of the favorite folder.
     */
    @ColumnInfo(name = "folder_name")
    private String folderName;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating when the folder was created in the local database.
     */
    @ColumnInfo(name = "created_at")
    private Date createdAt;

    /**
     * Timestamp indicating the last time the folder's data was updated.
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
     * Synchronization status of the folder with the remote server.
     * 0 for un-synced, 1 for synced, 2 for deleted.
     */
    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}
