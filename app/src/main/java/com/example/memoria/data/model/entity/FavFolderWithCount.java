package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class FavFolderWithCount {
    @Embedded
    public FavFolder folder; // Tự động map toàn bộ thông tin của bảng fav_folders

    @ColumnInfo(name = "word_count")
    public int wordCount; // Hứng cột đếm số lượng từ (COUNT)
}