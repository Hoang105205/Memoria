package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

import com.google.firebase.firestore.Exclude;

@Entity(tableName = "fav_folders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavFolder {
    @PrimaryKey
    @ColumnInfo(name = "folder_id")
    @androidx.annotation.NonNull
    @Exclude
    private UUID folderId;

    @ColumnInfo(name = "folder_name")
    private String folderName;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;

    @Exclude
    public UUID getFolderId() {
        return folderId;
    }

    @Exclude
    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }
}
