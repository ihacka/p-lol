package io.ilot.plol.model.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @Enumerated(EnumType.STRING)
    private CardType cardType;
    private Integer awardedNoOfUses;
    private Integer noOfUses;

    public @Transient int getRemainingUses(){
        return awardedNoOfUses - noOfUses;
    }

    public int playCard(){
        if(getRemainingUses()>0) awardedNoOfUses--;
        return getRemainingUses();
    }

    public static Set<Card> allCards(){
        return Stream.of(CardType.values())
                .map(cardType -> new Card(cardType, 1, 0))
                .collect(Collectors.toSet());
    }

    public boolean equals(Card card){
        if (null == card) return false;
        return cardType.equals(card.getCardType());
    }
}
