package com.models;

import jakarta.persistence.*;

@Entity
@Table(name = "basketItems")
public class BasketItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long foodId;

    @Column
    private String foodName;

    @Column
    private double price;

    @Column
    private int quantity;

   @Column(nullable = false)
    private Long basketId;

    public BasketItem() {}
}
