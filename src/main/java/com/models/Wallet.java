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
    public boolean removeAmount(double amount) {
        if(balance - amount >= 0) {
            this.balance -= amount;
            return true;
        }else{
            return false;
        }
    }
}
