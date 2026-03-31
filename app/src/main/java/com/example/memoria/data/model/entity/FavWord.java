package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

import com.google.firebase.firestore.Exclude;

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
    @PrimaryKey
    @ColumnInfo(name = "fav_id")
    @androidx.annotation.NonNull
    @Exclude
    private UUID favId;

    @ColumnInfo(name = "folder_id", index = true)
    @Exclude
    private UUID folderId;

    @ColumnInfo(name = "word_text")
    private String wordText;

    @ColumnInfo(name = "part_of_speech")
    private String partOfSpeech;

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

    @Exclude
    public UUID getFavId() {
        return favId;
    }

    @Exclude
    public void setFavId(UUID favId) {
        this.favId = favId;
    }

    @Exclude
    public UUID getFolderId() {
        return folderId;
    }

    @Exclude
    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }
}
