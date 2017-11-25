package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Event {
    @Id
    private Long id;
    private String name;
    private String homeName;
    private String awayName;
    private String betStatus;
    private Long kickOff;
    private String matchTime;
    private Integer redCardsHome;
    private Integer redCardsAway;
    private Integer yellowCardsHome;
    private Integer yellowCardsAway;
    private Integer cornersHome;
    private Integer cornersAway;
    private String score;
    private String period;
    private Integer freeKicksHome;
    private Integer freeKicksAway;
    private Integer goalKicksHome;
    private Integer goalKicksAway;
    private Integer dangerousAttacksHome;
    private Integer dangerousAttacksAway;
    private Integer scoreHome;
    private Integer scoreAway;
    @Transient
    private MarketLiveOdds odds;


    @OneToMany(mappedBy = "event")
    private List<Market> markets;

    public void setName(){
        this.name = homeName+" - "+awayName;
    }
}
