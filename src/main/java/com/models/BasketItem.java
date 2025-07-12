package com.models;

import jakarta.persistence.*;

@Entity
@Table(name = "basket_items")
public class BasketItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long buyer_id;

    @Column
    private Long food_item_id;

    @Column
    private int quantity;

    @Column
    private String name;

    @Column
    private int price;

    @Column
    private Long vendorId;

    @Column
    private String imageBase64;


    public BasketItem() {}

    public void setBuyer_id(Long buyer_id) {
        this.buyer_id = buyer_id;
    }
    public void setFood_item_id(Long food_item_id) {
        this.food_item_id = food_item_id;
    }
    public void setQuantity(int quantity) {

        this.quantity = quantity;
    }
    public void setName(String name) { this.name = name; }
    public void setPrice(int price) { this.price = price; }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
}
