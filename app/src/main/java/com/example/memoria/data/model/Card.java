package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(
        tableName = "cards",
        foreignKeys = @ForeignKey(
                entity = Deck.class,
                parentColumns = "deck_id",
                childColumns = "deck_id",
                onDelete = ForeignKey.CASCADE // Xóa Deck thì xóa luôn các Card
        )
)
@Data
public class Card {
    @PrimaryKey
    @ColumnInfo(name = "card_id")
    @androidx.annotation.NonNull
    private UUID cardId;

    @ColumnInfo(name = "deck_id", index = true) // index cho khóa ngoại để truy vấn nhanh
    private UUID deckId;

    @ColumnInfo(name = "front_text")
    private String frontText;

    @ColumnInfo(name = "front_image")
    private String frontImage; // Lưu đường dẫn file ảnh

    @ColumnInfo(name = "back_types")
    private List<String> backTypes;

    @ColumnInfo(name = "back_meanings")
    private List<String> backMeanings;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "ease_factor")
    private float easeFactor;

    @ColumnInfo(name = "interval_days")
    private int intervalDays;

    @ColumnInfo(name = "last_result")
    private float lastResult;

    @ColumnInfo(name = "last_review_at")
    private Date lastReviewAt;

    @ColumnInfo(name = "review_count")
    private int reviewCount;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}