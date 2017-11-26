package io.ilot.plol.model.board;

import io.ilot.plol.model.Bet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Board {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Member> members= new ArrayList<>();

    private BigDecimal getPool(){
        return members.stream()
                .map(member -> member.getOpenAmount())
                .reduce(BigDecimal.ZERO, (b1,b2)-> b1.add(b2));
    }

    private Long getOpenBets(){
        return members.stream()
                .flatMap(m -> m.getPlacedBets().stream())
                .filter(bet -> bet.getStatus().equals(Bet.BetStatus.ACCEPTED))
                .count();
    }

    public void addMember(Member member){
        members.add(member);
        member.setBoard(this);
    }

}
