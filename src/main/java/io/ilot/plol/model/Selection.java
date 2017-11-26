package io.ilot.plol.model;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
    private Boolean finalOutcome;

    @ManyToOne
    @JoinColumn(name = "market_id")
    private Market market;

    public  Selection(String name, Float odd, Boolean active, Boolean finalOutcome){
        this.name = name;
        this.odd=odd;
        this.active= active;
        this.finalOutcome = finalOutcome;
    }
//    of.getType(), of.getValue(), of.isActive(), of.isFinalOutcome()
}