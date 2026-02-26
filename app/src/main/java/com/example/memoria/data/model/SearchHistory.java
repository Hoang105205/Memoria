package com.example.memoria.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "search_histories")
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
