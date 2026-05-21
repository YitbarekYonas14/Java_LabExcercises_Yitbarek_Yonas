package PokerGame;
public class Card {
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }
    public enum Rank {
        TWO(2,"2"), THREE(3,"3"), FOUR(4,"4"), FIVE(5,"5"), SIX(6,"6"),
        SEVEN(7,"7"), EIGHT(8,"8"), NINE(9,"9"), TEN(10,"10"),
        JACK(11,"J"), QUEEN(12,"Q"), KING(13,"K"), ACE(14,"A");
        public final int value;
        public final String symbol;
        Rank(int v, String s) { this.value = v; this.symbol = s; }
    }

    public final Rank rank;
    public final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String suitSymbol() {
        switch (suit) {
            case CLUBS:    return "♣";
            case DIAMONDS: return "♦";
            case HEARTS:   return "♥";
            case SPADES:   return "♠";
            default:       return "?";
        }
    }

    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }

    @Override
    public String toString() {
        return rank.symbol + suitSymbol();
    }
}
