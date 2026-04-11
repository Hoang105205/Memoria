package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "quiz_stat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizStat implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "stat_id")
    private int statId; // Chỉ có 1 dòng duy nhất, set cứng ID = 1

    @ColumnInfo(name = "total_quiz")
    private int totalQuiz;

    @ColumnInfo(name = "total_correct")
    private int totalCorrect;

    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}