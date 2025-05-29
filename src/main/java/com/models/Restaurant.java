package com.models;

import jakarta.persistence.*;

@Entity
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
    @Column(nullable = false)
   private String name;
    @Column(nullable = false)
   private String address;
    @Column(nullable = false)
   private String phone;
    @Column
   private String logoBase64;
    @Column(nullable = false)
   private String tax_fee;
    @Column(nullable = false)
   private int additional_fee;
    @Column(nullable = false)
    private Long ownerId;
    public String getTax_fee() {
        return tax_fee;
    }
    public Long getOwnerId() {
        return ownerId;
    }
}
