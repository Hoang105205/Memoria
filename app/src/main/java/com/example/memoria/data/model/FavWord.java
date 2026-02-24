package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

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
public class FavWord {
    @PrimaryKey
    @ColumnInfo(name = "fav_id")
    @androidx.annotation.NonNull
    private UUID favId;

    @ColumnInfo(name = "folder_id", index = true)
    private UUID folderId;

    @ColumnInfo(name = "word_text")
    private String wordText;

    @ColumnInfo(name = "short_meaning")
    private String shortMeaning;

    @ColumnInfo(name = "added_at")
    private Date addedAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "pin_status")
    private boolean pinStatus;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}
