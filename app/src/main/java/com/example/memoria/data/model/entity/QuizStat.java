package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity class representing overall quiz statistics in the Memoria application.
 * Typically maintains a single row to aggregate the user's total learning progress.
 */
@Entity(tableName = "quiz_stat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizStat implements Serializable {

    // ========================================================================
    // Basic Information
    // ========================================================================

    /**
     * Unique identifier for the statistics record.
     * Usually hardcoded to 1 since there is only one global statistics row for the user.
     */
    @PrimaryKey
    @ColumnInfo(name = "stat_id")
    private int statId;

    // ========================================================================
    // Statistics Data
    // ========================================================================

    /**
     * The total number of quizzes the user has taken.
     */
    @ColumnInfo(name = "total_quiz")
    private int totalQuiz;

    /**
     * The total number of correct answers across all quizzes.
     */
    @ColumnInfo(name = "total_correct")
    private int totalCorrect;

    /**
     * The total number of questions answered across all quizzes.
     */
    @ColumnInfo(name = "total_questions")
    private int totalQuestions;

    // ========================================================================
    // Date Information
    // ========================================================================

    /**
     * Timestamp indicating the last time these statistics were updated.
     */
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    // ========================================================================
    // Firestore & Sync Status
    // ========================================================================

    /**
     * The corresponding Document ID in Firebase Firestore.
     */
    @ColumnInfo(name = "firestore_id")
    private String firestoreId;

    /**
     * Synchronization status of the statistics with the remote server.
     * 0 for un-synced, 1 for synced, 2 for deleted.
     */
    @ColumnInfo(name = "sync_status")
    private int syncStatus;
}