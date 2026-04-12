package com.example.memoria.data.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

/**
 * A Room projection class used to retrieve a Deck along with its calculated total card count.
 * Note: This class is NOT annotated with @Entity because it does not represent a standalone
 * table in the database. It is simply a data container to capture the combined results
 * of a custom DAO query (e.g., a query involving a JOIN or COUNT operation).
 */
public class DeckWithCount {

    /**
     * The embedded Deck entity.
     * The @Embedded annotation instructs Room to automatically map all columns from the
     * underlying 'decks' table directly into this object's fields.
     */
    @Embedded
    public Deck deck;

    /**
     * The calculated total number of cards within the deck.
     * The @ColumnInfo(name = "total_cards") annotation maps this field to the
     * 'total_cards' alias generated in the SQL query.
     */
    @ColumnInfo(name = "total_cards")
    public int totalCards;
}