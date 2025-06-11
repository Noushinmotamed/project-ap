package com.models;
import jakarta.persistence.*;

/**
 * the list of basketItems was removed. now each basket item holds the id of basket in it instead
 * to avoid keeping basket object in each basketItem and oneToMany relation
 */
@Entity
@Table(name = "baskets")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long basketId;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column( nullable = false)
    private double totalPrice;

    public Basket() {}

    public Basket(Long userId) {
        this.userId = userId;
        totalPrice = 0;

    }
    public void setUserId(Long userId) {

        this.userId = userId;
    }
}
