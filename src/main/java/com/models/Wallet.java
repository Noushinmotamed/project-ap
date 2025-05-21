package com.models;

import jakarta.persistence.*;

@Entity
@Table( name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private long userId;

    @Column( nullable = false)
    private double balance;

    public Wallet() {}

    public Wallet(long userId) {
        this.userId = userId;
        this.balance = 0;
    }
    public void addAmount(double amount) {
        this.balance += amount;
    }
}
