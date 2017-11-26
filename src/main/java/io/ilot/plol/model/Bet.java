package io.ilot.plol.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Entity
@Data
@NoArgsConstructor
public class Bet {
    @Id
    @GeneratedValue
    private Long id;
    private Long selectionId;
    private BigDecimal stake;
    @Transient
    @Setter(AccessLevel.NONE) private BigDecimal payout;
    private BigDecimal winnings;
    private Date placedTime = new Date();
    @OneToOne
    private User user;
    private BetStatus status;

    public BigDecimal getPayout() {
        return Optional.ofNullable(payout).map(p -> p.subtract(stake)).orElse(BigDecimal.ZERO);
    }

    public enum BetStatus {
        ACCEPTED, REJECTED, CANCELLED, SETTLED; //CASHED_OUT
    }
}
