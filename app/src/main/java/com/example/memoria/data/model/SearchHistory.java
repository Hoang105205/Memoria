package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Entity(
        tableName = "search_histories",
        indices = {@Index(value = {"word_text"}, unique = true)}
)
@Data
public class SearchHistory {
    @PrimaryKey
    @ColumnInfo(name = "history_id")
    @androidx.annotation.NonNull
    private UUID historyId;

    @ColumnInfo(name = "word_text")
    private String wordText;

    @ColumnInfo(name = "searched_at")
    private Date searchedAt;
}