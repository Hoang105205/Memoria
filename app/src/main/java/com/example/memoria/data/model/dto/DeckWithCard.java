package com.example.memoria.data.model.dto;

import com.example.memoria.data.model.entity.Card;
import com.example.memoria.data.model.entity.Deck;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckWithCard {
    private Deck deck;
    private List<Card> cards;
}