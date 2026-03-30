package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import java.util.Date;

@Entity(tableName = "quiz_stat")
@Data
public class QuizStat {
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
}