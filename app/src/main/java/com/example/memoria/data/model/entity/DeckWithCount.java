package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class DeckWithCount {
    @Embedded
    public Deck deck; // Tự động map toàn bộ cột của bảng decks

    @ColumnInfo(name = "total_cards")
    public int totalCards; // Hứng cột đếm tổng số thẻ
}