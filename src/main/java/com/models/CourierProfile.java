package com.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table( name = "courtierprofiles")
public class CourierProfile extends Profile {

    @Column
    private String bankInfo;

    @Column
    private CourierStatus status;

    public CourierProfile() {}
    public CourierProfile(Long id, String fullName, String phoneNumber) {
        super(id, fullName, phoneNumber);
        status = CourierStatus.AVAILABLE;
    }
    public String getBankInfo() {
        return bankInfo;
    }
    public CourierStatus getStatus() {
        return status;
    }
    public void setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }
    public void setStatus(CourierStatus status) {
        this.status = status;
    }
}
