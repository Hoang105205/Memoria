package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(
        tableName = "quiz_his",
        foreignKeys = @ForeignKey(
                entity = Deck.class,
                parentColumns = "deck_id",
                childColumns = "deck_id",
                onDelete = ForeignKey.CASCADE // Xóa Deck thì xóa luôn lịch sử thi của Deck đó
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizHistory implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "result_id")
    @androidx.annotation.NonNull
    @Exclude
    private UUID resultId;

    @ColumnInfo(name = "deck_id", index = true)
    @Exclude
    private UUID deckId;

    @ColumnInfo(name = "correct_count")
    private int correctCount;

    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    @ColumnInfo(name = "taken_at")
    private Date takenAt;

    @ColumnInfo(name = "expire_at")
    private Date expireAt;

    @ColumnInfo(name = "time_taken")
    private int timeTaken;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;

    @Exclude
    public UUID getResultId() { return resultId; }

    @Exclude
    public void setResultId(UUID resultId) { this.resultId = resultId; }

    @Exclude
    public UUID getDeckId() { return deckId; }

    @Exclude
    public void setDeckId(UUID deckId) { this.deckId = deckId; }

    @PropertyName("deck_id_string")
    public String getFirestoreDeckId() {
        return deckId != null ? deckId.toString() : null;
    }

    @PropertyName("deck_id_string")
    public void setFirestoreDeckId(String idStr) {
        this.deckId = idStr != null ? UUID.fromString(idStr) : null;
    }
}