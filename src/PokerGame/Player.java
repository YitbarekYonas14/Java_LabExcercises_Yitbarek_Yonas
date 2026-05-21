package PokerGame;

import java.util.ArrayList;

public class Player {
    public String name;
    public int chips;
    public int raiseAmount;
    public boolean isBot;
    public ArrayList<Card> hand = new ArrayList<>();
    public boolean folded = false;
    public boolean allIn = false;
    public int currentBet = 0; 
    public Player(String name, int chips, int raiseAmount, boolean isBot) {
        this.name = name;
        this.chips = chips;
        this.raiseAmount = raiseAmount;
        this.isBot = isBot;
    }

    public void resetForNewRound() {
        hand.clear();
        folded = false;
        allIn = false;
        currentBet = 0;
    }

    public void resetBetForNewStreet() {
        currentBet = 0;
    }

    public boolean canAct() {
        return !folded && !allIn && chips > 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
