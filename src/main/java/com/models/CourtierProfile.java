package com.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table( name = "courtierprofiles")
public class CourtierProfile extends Profile {

    @Column
    private String bankInfo;

    @Column
    private CourtierStatus status;

    public CourtierProfile() {}
    public CourtierProfile(Long id, String fullName, String phoneNumber) {
        super(id, fullName, phoneNumber);
        status = CourtierStatus.AVAILABLE;
    }
}
