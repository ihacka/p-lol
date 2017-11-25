package io.ilot.plol.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private BigDecimal balance;

    public User(String name, String email, BigDecimal balance) {
        this.name = name;
        this.email = email;
        this.balance = balance;
    }

    public BigDecimal removeFromBalance(BigDecimal amount){
        if(amount.compareTo(balance) > 1) throw new IllegalArgumentException("Insufficient funds");
        setBalance(balance.subtract(amount));
        return balance;
    }
}
