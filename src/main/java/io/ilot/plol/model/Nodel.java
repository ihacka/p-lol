package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

public class Nodel {

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


    @Entity @Data @NoArgsConstructor public static class Board {
        @Id @GeneratedValue private Long id;
        @ManyToOne
        private List<Member> members;

//        private BigDecimal getPool(){
//            return members.stream()
//                    .map(member -> member.getPlacedBets().stream()
//                                .map(bet -> bet.) )
//        }
    }

    @Data @NoArgsConstructor public static class Member {
        @Id @GeneratedValue private Long id;
        @ManyToOne
        private Board board;
        @ManyToOne
        private User user;
        private List<Bet> placedBets;


//        private BigDecimal getOpenAmount() {
//            placedBets.stream()
//                    .filter(bet -> )
//        }
        @Transient private BigDecimal potentialWinnings;
    }

    @Data @NoArgsConstructor public static class Card {
        private CardType cardType;

        private int awardedNoOfUses;
        private int noOfUses;

        public int getRemainingUses(){
            return awardedNoOfUses - noOfUses;
        }
    }
}
