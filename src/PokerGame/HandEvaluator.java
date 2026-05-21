package PokerGame;

import java.util.*;

public class HandEvaluator {

    public enum HandRank {
        HIGH_CARD(0, "High Card"),
        PAIR(1, "One Pair"),
        TWO_PAIR(2, "Two Pair"),
        THREE_OF_A_KIND(3, "Three of a Kind"),
        STRAIGHT(4, "Straight"),
        FLUSH(5, "Flush"),
        FULL_HOUSE(6, "Full House"),
        FOUR_OF_A_KIND(7, "Four of a Kind"),
        STRAIGHT_FLUSH(8, "Straight Flush"),
        ROYAL_FLUSH(9, "Royal Flush");

        public final int value;
        public final String label;
        HandRank(int v, String l) { this.value = v; this.label = l; }
    }

    public static class EvalResult {
        public HandRank rank;
        public int[] tiebreakers; 
        public EvalResult(HandRank r, int[] t) { rank = r; tiebreakers = t; }
    }

    
    public static EvalResult evaluate(List<Card> cards) {
        EvalResult best = null;
        List<Card> list = new ArrayList<>(cards);
        int n = list.size();
        if (n < 5) return evalFive(list);
        
        for (int i = 0; i < n-4; i++)
            for (int j = i+1; j < n-3; j++)
                for (int k = j+1; k < n-2; k++)
                    for (int l = k+1; l < n-1; l++)
                        for (int m = l+1; m < n; m++) {
                            List<Card> five = Arrays.asList(list.get(i),list.get(j),list.get(k),list.get(l),list.get(m));
                            EvalResult r = evalFive(five);
                            if (best == null || compare(r, best) > 0) best = r;
                        }
        return best;
    }

    private static EvalResult evalFive(List<Card> cards) {
        int[] ranks = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) ranks[i] = cards.get(i).rank.value;
        Arrays.sort(ranks);
        
        for (int i = 0, j = ranks.length-1; i < j; i++, j--) { int t=ranks[i]; ranks[i]=ranks[j]; ranks[j]=t; }

        boolean flush = isFlush(cards);
        boolean straight = isStraight(ranks);
        int straightHigh = getStraightHigh(ranks);

        HashMap<Integer,Integer> freq = new HashMap<>();
        for (int r : ranks) freq.merge(r, 1, Integer::sum);

        List<Map.Entry<Integer,Integer>> entries = new ArrayList<>(freq.entrySet());

        entries.sort((a,b) -> a.getValue().equals(b.getValue()) ? b.getKey()-a.getKey() : b.getValue()-a.getValue());

        int[] counts = entries.stream().mapToInt(Map.Entry::getValue).toArray();
        int[] ranksSorted = entries.stream().mapToInt(Map.Entry::getKey).toArray();

        if (flush && straight) {
            if (straightHigh == 14) return new EvalResult(HandRank.ROYAL_FLUSH, new int[]{14});
            return new EvalResult(HandRank.STRAIGHT_FLUSH, new int[]{straightHigh});
        }
        if (counts[0] == 4) return new EvalResult(HandRank.FOUR_OF_A_KIND, new int[]{ranksSorted[0], ranksSorted[1]});
        if (counts[0] == 3 && counts[1] == 2) return new EvalResult(HandRank.FULL_HOUSE, new int[]{ranksSorted[0], ranksSorted[1]});
        if (flush) return new EvalResult(HandRank.FLUSH, ranks);
        if (straight) return new EvalResult(HandRank.STRAIGHT, new int[]{straightHigh});
        if (counts[0] == 3) return new EvalResult(HandRank.THREE_OF_A_KIND, new int[]{ranksSorted[0], ranksSorted[1], ranksSorted[2]});
        if (counts[0] == 2 && counts[1] == 2) return new EvalResult(HandRank.TWO_PAIR, new int[]{ranksSorted[0], ranksSorted[1], ranksSorted[2]});
        if (counts[0] == 2) return new EvalResult(HandRank.PAIR, new int[]{ranksSorted[0], ranksSorted[1], ranksSorted[2], ranksSorted[3]});
        return new EvalResult(HandRank.HIGH_CARD, ranks);
    }

    private static boolean isFlush(List<Card> cards) {
        Card.Suit s = cards.get(0).suit;
        for (Card c : cards) if (c.suit != s) return false;
        return true;
    }

    private static boolean isStraight(int[] sortedDesc) {

        boolean ok = true;
        for (int i = 1; i < sortedDesc.length; i++)
            if (sortedDesc[i-1] - sortedDesc[i] != 1) { ok = false; break; }
        if (ok) return true;

        if (sortedDesc[0]==14 && sortedDesc[1]==5 && sortedDesc[2]==4 && sortedDesc[3]==3 && sortedDesc[4]==2) return true;
        return false;
    }

    private static int getStraightHigh(int[] sortedDesc) {
        if (sortedDesc[0]==14 && sortedDesc[1]==5) return 5; // wheel
        return sortedDesc[0];
    }


    public static int compare(EvalResult a, EvalResult b) {
        if (a.rank.value != b.rank.value) return a.rank.value - b.rank.value;
        for (int i = 0; i < Math.min(a.tiebreakers.length, b.tiebreakers.length); i++) {
            if (a.tiebreakers[i] != b.tiebreakers[i]) return a.tiebreakers[i] - b.tiebreakers[i];
        }
        return 0;
    }


    public static List<Player> findWinners(List<Player> activePlayers, List<Card> community) {
        List<Player> winners = new ArrayList<>();
        EvalResult bestResult = null;
        for (Player p : activePlayers) {
            if (p.folded) continue;
            List<Card> allCards = new ArrayList<>(p.hand);
            allCards.addAll(community);
            EvalResult res = evaluate(allCards);
            if (bestResult == null || compare(res, bestResult) > 0) {
                bestResult = res;
                winners.clear();
                winners.add(p);
            } else if (compare(res, bestResult) == 0) {
                winners.add(p);
            }
        }
        return winners;
    }

    public static EvalResult getBestResult(Player p, List<Card> community) {
        List<Card> allCards = new ArrayList<>(p.hand);
        allCards.addAll(community);
        return evaluate(allCards);
    }
}
