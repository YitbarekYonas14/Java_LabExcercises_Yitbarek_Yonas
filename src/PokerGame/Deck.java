package PokerGame;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> cards = new ArrayList<>();

    public Deck() {
        reset();
    }

    public void reset() {
        cards.clear();
        for (Card.Suit suit : Card.Suit.values())
            for (Card.Rank rank : Card.Rank.values())
                cards.add(new Card(rank, suit));
        Collections.shuffle(cards);
    }

    public Card deal() {
        if (cards.isEmpty()) throw new RuntimeException("Deck empty");
        return cards.remove(cards.size() - 1);
    }

    public int remaining() {
        return cards.size();
    }
}
