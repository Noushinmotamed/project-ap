package com.models;

import jakarta.persistence.*;

@Entity
@Table( name = "adminProfiles")
public class AdminProfile extends Profile {
    public AdminProfile() {}
    public AdminProfile(Long id, String fullName, String phoneNumber) {
        super(id, fullName, phoneNumber);
    }
}
