package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "fav_folders")
@Data
public class FavFolder {
    @PrimaryKey
    @ColumnInfo(name = "folder_id")
    @androidx.annotation.NonNull
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
}
