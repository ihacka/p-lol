package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String betStatus;
    private Long kickOff;
    private String matchTime;
    private Integer redCardsHome;
    private Integer redCardsAway;


    @OneToMany(mappedBy = "event")
    private List<Market> markets;
}
