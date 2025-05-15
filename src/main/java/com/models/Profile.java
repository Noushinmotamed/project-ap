package com.models;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private Long userId;

    @Column( nullable = false)
    private String fullName;

    @Column( nullable = false)
    private String phoneNumber;

    @Column( nullable =true)
    private String imageUrl;

    @Column( nullable = true, unique = false)
    private String address;

    public Profile() {}
    public Profile(Long userId, String fullName, String phoneNumber) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;

    }
    public Profile(Long userId, String fullName, String phoneNumber,String address) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

}
