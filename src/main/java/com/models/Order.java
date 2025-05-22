package com.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;
    @Column(nullable = false)
    private Long customerId;
    @Column(nullable = false)
    private Long restaurantId;
    @Column
    private Long sellerId;
    @Column
    private Long courierId;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
    @Column
    private String address;
    @Column( nullable = false)
    private double totalPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Column(nullable = false)
    private LocalDateTime dateTime;
    public Long getCustomerId() {
        return customerId;
    }
    public void setStatus( OrderStatus status ) {
        this.status = status;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public Long getSellerId(){
        return sellerId;
    }
    public OrderStatus getStatus(){
        return status;
    }

}
