package com.rabobank.bankservice.entity;

import com.rabobank.bankservice.error.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal balance;

    @OneToOne(cascade = CascadeType.ALL)
    private Card card;

    public void subtractBalance(BigDecimal amount) {
        BigDecimal newBalance = this.balance.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException(String.format("Cannot subtract %s. Current balance: %s. Resulting balance would be negative.", amount, this.balance));
        }

        this.balance = newBalance;
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}