package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import lombok.Data;
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
public class QuizHistory {
    @PrimaryKey
    @ColumnInfo(name = "result_id")
    @androidx.annotation.NonNull
    private UUID resultId;

    @ColumnInfo(name = "deck_id", index = true)
    private UUID deckId;

    @ColumnInfo(name = "correct_count")
    private int correctCount;

    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    @ColumnInfo(name = "taken_at")
    private Date takenAt;

    @ColumnInfo(name = "expire_at")
    private Date expireAt;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;
}
