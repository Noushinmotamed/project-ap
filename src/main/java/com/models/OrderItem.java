package com.models;

import jakarta.persistence.*;

@Entity
@Table(name = "orderItems")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId; // Primary key

    @Column(nullable = false)
    private Long foodId;

    @ManyToOne
    @JoinColumn(name = "orderId")
    private Order order;

    @Column(nullable = false)
    private String foodName;

    @Column(nullable = false)
    private double finalPrice;

    @Column(nullable = false)
    private int quantity;
}