package io.ilot.plol.model.board;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ilot.plol.model.Bet;
import io.ilot.plol.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne @JsonIgnore
    private Board board;
    @ManyToOne
    private User user;
    @OneToMany
    @JoinTable(
            name="MEMBER_BETS",
            joinColumns = @JoinColumn( name="member_id"),
            inverseJoinColumns = @JoinColumn( name="bet_id")
    )
    private List<Bet> placedBets;

    @ElementCollection
    @CollectionTable(name="MEMBER_CARDS", joinColumns=@JoinColumn(name="member_id"))
    private Set<Card> cards = Card.allCards();

    //--
    public Member(User user) {
        this.user = user;
    }

    //--
    @Transient private Predicate<Bet> openBets = bet -> bet.getStatus().equals(Bet.BetStatus.ACCEPTED);

    //--
    public void playCard(CardType cardType){
        cards.stream()
                .filter(c -> c.getCardType().equals(cardType))
                .forEach(c -> c.playCard());
    }

    public Long getOpenBets(){
        return placedBets.stream()
                .filter(openBets)
                .count();
    }

    public BigDecimal getOpenAmount() {
        return placedBets.stream()
                .filter(openBets)
                .map(bet -> bet.getStake())
                .reduce(BigDecimal.ZERO, (b1, b2) -> b1.add(b2));
    }

    public BigDecimal getPotentialWinnings() {
        return placedBets.stream()
                .filter(openBets)
                .map(bet -> bet.getWinnings())
                .reduce(BigDecimal.ZERO, (b1, b2) -> b1.add(b2));
    }
}
