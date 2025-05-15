package com.models;

import jakarta.persistence.*;

@Entity
@Table(name = "customerProfiles")
public class CustomerProfile extends Profile {
    public CustomerProfile() {}
    public CustomerProfile(String address,Long userId, String fullName, String phoneNumber) {
        super(userId, fullName, phoneNumber,address);
    }
}
