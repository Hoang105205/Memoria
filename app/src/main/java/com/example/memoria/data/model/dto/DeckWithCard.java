package com.example.memoria.data.model.dto;

import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * A Data Transfer Object (DTO) used to bundle a Deck with its complete list of associated Cards.
 * Useful for operations that require the entire deck structure at once, for preparing a deck for community publication,
 * cloning and sharing 1:1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckWithCard {

    // ========================================================================
    // Deck & Cards Data
    // ========================================================================

    /**
     * The parent Deck entity.
     */
    private Deck deck;

    /**
     * The complete list of Card entities belonging to this deck.
     */
    private List<Card> cards;
}