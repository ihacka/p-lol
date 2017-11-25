package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Selection {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Float odd;
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "market_id")
    private Market market;
}