package io.ilot.plol.model.board;

public enum CardType {
    ODDS_BOOST("Odds Boost"),
    FREEZE_BETTING("Freeze Betting for 5 mins"),
    FREEZE_CARD_PLAYING("Freeze Card Playing for 5 mins"),
    CANCEL_BET("Cancel a Bet"),
    CANCEL_CARD("Cancel Card"),
    WINNINGS_BLOW("Winnings blow");

    private String description;

    CardType(String description){
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
//public enum CardType {
//    ODDS_BOOST,
//    FREEZE_BETTING,
//    FREEZE_CARD_PLAYING,
//    CANCEL_BET,
//    CANCEL_CARD,
//    WINNINGS_BLOW
//
//}
