package PokerGame;

import java.util.*;

public class GameController {

    public enum Street { PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN }

    public ArrayList<Player> players;
    public ArrayList<Card> community = new ArrayList<>();
    public Deck deck = new Deck();
    public int pot = 0;
    public int currentBet = 0;
    public Street street = Street.PRE_FLOP;
    public int dealerIndex = 0;
    public int currentPlayerIndex = 0;
    public boolean handOver = false;
    public String statusMessage = "";
    public String lastBotAction = "";

    private int actionsThisStreet = 0;

    public Runnable onStateChanged;
    public Runnable onHandOver;
    public Runnable onBotAction;

    public List<Player> winners = new ArrayList<>();
    public HandEvaluator.HandRank winningRank;

    private Random rng = new Random();

    public GameController(ArrayList<Player> players) {
        this.players = players;
    }

    public void startNewHand() {
        deck.reset();
        community.clear();
        pot = 0;
        currentBet = 0;
        handOver = false;
        street = Street.PRE_FLOP;
        lastBotAction = "";
        winners.clear();
        actionsThisStreet = 0;

        for (Player p : players) p.resetForNewRound();

        for (Player p : players) {
            p.hand.add(deck.deal());
            p.hand.add(deck.deal());
        }


        int sb = nextActive(dealerIndex);
        int bb = nextActive(sb);
        postBlind(players.get(sb), 25);
        postBlind(players.get(bb), 50);
        currentBet = 50;

       
        currentPlayerIndex = nextActive(bb);
        statusMessage = "Pre-Flop. " + currentPlayer().name + "'s turn.";
        notifyChanged();
        if (currentPlayer().isBot) scheduleBotAction();
    }

    private void postBlind(Player p, int amount) {
        int actual = Math.min(amount, p.chips);
        p.chips -= actual;
        p.currentBet += actual;
        pot += actual;
    }

    public Player currentPlayer() { return players.get(currentPlayerIndex); }

    

    public void humanFold() {
        currentPlayer().folded = true;
        statusMessage = currentPlayer().name + " folds.";
        actionsThisStreet++;
        advance();
    }

    public void humanCall() {
        Player p = currentPlayer();
        int toCall = Math.min(currentBet - p.currentBet, p.chips);
        p.chips -= toCall; p.currentBet += toCall; pot += toCall;
        if (p.chips == 0) p.allIn = true;
        statusMessage = p.name + " calls $" + currentBet + ".";
        actionsThisStreet++;
        advance();
    }

    public void humanRaise() {
        Player p = currentPlayer();
        int toCall = Math.max(0, currentBet - p.currentBet);
        int total  = Math.min(toCall + p.raiseAmount, p.chips);
        p.chips -= total; p.currentBet += total; pot += total;
        if (p.currentBet > currentBet) { currentBet = p.currentBet; actionsThisStreet = 1; }
        else actionsThisStreet++;
        if (p.chips == 0) p.allIn = true;
        statusMessage = p.name + " raises to $" + currentBet + ".";
        advance();
    }

    public void humanCheck() {
        statusMessage = currentPlayer().name + " checks.";
        actionsThisStreet++;
        advance();
    }

    

    private void scheduleBotAction() {
        new Thread(() -> {
            try { Thread.sleep(900); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(this::doBotAction);
        }).start();
    }

    private void doBotAction() {
        if (handOver) return;
        Player p = currentPlayer();
        if (!p.isBot) return;

        int toCall   = currentBet - p.currentBet;
        boolean canCall  = toCall <= p.chips && toCall > 0;
        boolean canRaise = (Math.max(0, toCall) + p.raiseAmount) <= p.chips;
        int roll = rng.nextInt(100);

        if (toCall == 0) {
            if (canRaise && roll < 30) { doBotRaise(p); }
            else { lastBotAction = p.name + " checks."; statusMessage = lastBotAction; actionsThisStreet++; }
        } else {
            if (roll < 20) {
                p.folded = true;
                lastBotAction = p.name + " folds."; statusMessage = lastBotAction; actionsThisStreet++;
            } else if (canRaise && roll < 40) {
                doBotRaise(p);
            } else if (canCall) {
                int actual = Math.min(toCall, p.chips);
                p.chips -= actual; p.currentBet += actual; pot += actual;
                if (p.chips == 0) p.allIn = true;
                lastBotAction = p.name + " calls $" + currentBet + "."; statusMessage = lastBotAction; actionsThisStreet++;
            } else {
                p.folded = true;
                lastBotAction = p.name + " folds (can't afford)."; statusMessage = lastBotAction; actionsThisStreet++;
            }
        }
        if (onBotAction != null) onBotAction.run();
        advance();
    }

    private void doBotRaise(Player p) {
        int toCall = Math.max(0, currentBet - p.currentBet);
        int total  = Math.min(toCall + p.raiseAmount, p.chips);
        p.chips -= total; p.currentBet += total; pot += total;
        if (p.currentBet > currentBet) { currentBet = p.currentBet; actionsThisStreet = 1; }
        else actionsThisStreet++;
        if (p.chips == 0) p.allIn = true;
        lastBotAction = p.name + " raises to $" + currentBet + "."; statusMessage = lastBotAction;
    }

    
    private void advance() {
        long stillIn = players.stream().filter(pv -> !pv.folded).count();
        if (stillIn == 1) { endHandFold(); return; }
        if (isBettingRoundOver()) { nextStreet(); return; }

        currentPlayerIndex = nextActive(currentPlayerIndex);
        updateTurnStatus();
        notifyChanged();
        if (currentPlayer().isBot) scheduleBotAction();
    }

    private boolean isBettingRoundOver() {
        List<Player> eligible = new ArrayList<>();
        for (Player p : players)
            if (!p.folded && !p.allIn && p.chips > 0) eligible.add(p);
        if (eligible.isEmpty()) return true;
        for (Player p : eligible)
            if (p.currentBet < currentBet) return false;
        return actionsThisStreet >= eligible.size();
    }

    private void nextStreet() {
        for (Player p : players) p.resetBetForNewStreet();
        currentBet = 0;
        actionsThisStreet = 0;

        switch (street) {
            case PRE_FLOP:
                community.add(deck.deal()); community.add(deck.deal()); community.add(deck.deal());
                street = Street.FLOP; statusMessage = "── Flop dealt ──"; break;
            case FLOP:
                community.add(deck.deal()); street = Street.TURN; statusMessage = "── Turn dealt ──"; break;
            case TURN:
                community.add(deck.deal()); street = Street.RIVER; statusMessage = "── River dealt ──"; break;
            case RIVER:
                street = Street.SHOWDOWN; endHandShowdown(); return;
            default: break;
        }

        currentPlayerIndex = nextActive(dealerIndex);
        updateTurnStatus();
        notifyChanged();
        if (currentPlayer().isBot) scheduleBotAction();
    }

    private void updateTurnStatus() {
        String sn = street.name().replace("_", "-");
        statusMessage = "[" + sn + "] " + currentPlayer().name + "'s turn.";
    }

    
    private void endHandFold() {
        Player winner = players.stream().filter(p -> !p.folded).findFirst().orElse(null);
        if (winner != null) { winner.chips += pot; statusMessage = "🏆 " + winner.name + " wins $" + pot + " — all others folded!"; }
        handOver = true; advanceDealer(); notifyChanged();
        if (onHandOver != null) onHandOver.run();
    }

    private void endHandShowdown() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) if (!p.folded) active.add(p);
        winners = HandEvaluator.findWinners(active, community);
        int share = pot / winners.size();
        for (Player w : winners) w.chips += share;
        winningRank = HandEvaluator.getBestResult(winners.get(0), community).rank;
        if (winners.size() == 1) {
            statusMessage = "🏆 " + winners.get(0).name + " wins $" + pot + " with " + winningRank.label + "!";
        } else {
            StringBuilder sb = new StringBuilder("🤝 Split pot! ");
            for (Player w : winners) sb.append(w.name).append(" ");
            sb.append("— ").append(winningRank.label).append(" ($").append(share).append(" each)");
            statusMessage = sb.toString();
        }
        handOver = true; advanceDealer(); notifyChanged();
        if (onHandOver != null) onHandOver.run();
    }

   

    private int nextActive(int from) {
        int n = players.size();
        for (int i = 1; i <= n; i++) {
            int idx = (from + i) % n;
            Player p = players.get(idx);
            if (!p.folded && !p.allIn && p.chips > 0) return idx;
        }
        for (int i = 1; i <= n; i++) {
            int idx = (from + i) % n;
            if (!players.get(idx).folded) return idx;
        }
        return from;
    }

    private void advanceDealer() {
        int n = players.size();
        for (int i = 1; i <= n; i++) {
            int idx = (dealerIndex + i) % n;
            if (players.get(idx).chips > 0) { dealerIndex = idx; return; }
        }
    }

    private void notifyChanged() { if (onStateChanged != null) onStateChanged.run(); }

    public boolean isHumanTurn() { return !handOver && !currentPlayer().isBot && currentPlayer().canAct(); }
    public boolean canCheck() { Player p = currentPlayer(); return currentBet == 0 || p.currentBet >= currentBet; }
    public boolean canCall()  { Player p = currentPlayer(); return currentBet > p.currentBet && p.chips > 0; }
    public boolean canRaise() { Player p = currentPlayer(); return p.chips > Math.max(0, currentBet - p.currentBet); }
    public void removeBankrupt() { players.removeIf(p -> p.chips <= 0); }
}
