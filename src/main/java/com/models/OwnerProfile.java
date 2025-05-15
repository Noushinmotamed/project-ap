package com.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table( name = "ownerProfiles")
public class OwnerProfile extends Profile {

    @Column
    private String bankInfo;

    @Column
    private String brandName;

    @Column
    private String explanation;

    public OwnerProfile() {}

    public OwnerProfile(String address,long userId, String fullName, String phoneNumber) {
        super(userId, fullName, phoneNumber,address);
    }
    public void setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
