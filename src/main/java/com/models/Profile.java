package com.models;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private Long userId;

    @Column( nullable = true)
    private String fullName;

    @Column( nullable = true, unique = true)
    private String phone;

    @Column( nullable =true)
    private String imageUrl;

    @Column( nullable = true, unique = false)
    private String address;

    @Column
    private String email;

    public Profile() {}
    public Profile(Long userId, String fullName, String phone) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;

    }
    public Profile(Long userId, String fullName, String phone, String address) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setPhone(String phoneNumber) { this.phone = phoneNumber; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getFullName() { return fullName; }

    public String getPhone() { return phone; }

    public String getAddress() { return address; }

    public String getImageUrl() { return imageUrl; }



}
