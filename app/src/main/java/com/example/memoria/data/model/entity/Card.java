package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.firebase.firestore.Exclude;

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
@NoArgsConstructor
@AllArgsConstructor
public class Card implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "card_id")
    @androidx.annotation.NonNull
    @Exclude
    private UUID cardId;

    @ColumnInfo(name = "deck_id", index = true) // index cho khóa ngoại để truy vấn nhanh
    @Exclude
    private UUID deckId;

    @ColumnInfo(name = "card_type")
    private int cardType; // 0: Text, 1: Image, 2: Audio

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
    private double easeFactor;

    @ColumnInfo(name = "interval_days")
    private int intervalDays;

    @ColumnInfo(name = "last_result")
    private float lastResult;

    @ColumnInfo(name = "last_review_at")
    private Date lastReviewAt;

    @ColumnInfo(name = "next_review_date")
    private Date nextReviewDate;

    @ColumnInfo(name = "review_count")
    private int reviewCount;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;

    @Exclude
    public UUID getCardId() {
        return cardId;
    }

    @Exclude
    public void setCardId(UUID cardId) {
        this.cardId = cardId;
    }

    @Exclude
    public UUID getDeckId() {
        return deckId;
    }

    @Exclude
    public void setDeckId(UUID deckId) {
        this.deckId = deckId;
    }
    @Exclude
    public String getFrontText() {
        return frontText;
    }
    @Exclude
    public List<String> getBackMeanings() {
        return backMeanings;
    }
}