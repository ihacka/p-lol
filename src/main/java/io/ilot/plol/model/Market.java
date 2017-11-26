package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Market {
    @Id
    private Long id;
    private String name;
    private boolean active;

    @OneToMany
    private List<Selection> selections;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;


}
