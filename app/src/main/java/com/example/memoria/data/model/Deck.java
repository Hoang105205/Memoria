package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "decks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    @PrimaryKey
    @ColumnInfo(name = "deck_id")
    @androidx.annotation.NonNull
    private UUID deckId;

    @ColumnInfo(name = "deck_name")
    private String deckName;

    @ColumnInfo(name = "cover_color")
    private String coverColor;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}