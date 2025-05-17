package com.controller;

import com.models.CourtierProfile;
import com.models.CourtierStatus;

public class CourtierProfileDTO extends ProfileDTO {
    private String bankInfo;
    private CourtierStatus courtierStatus;
    public CourtierProfileDTO(String fullName, String phoneNumber, String address, String imageUrl, String email,String bankInfo,CourtierStatus courtierStatus) {
        super(fullName,phoneNumber,address,imageUrl,email);
        this.bankInfo = bankInfo;
        this.courtierStatus = courtierStatus;
    }
    public String getBankInfo() {
        return bankInfo;
    }


    public void setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }

    public CourtierStatus getCourtierStatus() {
        return courtierStatus;
    }
}

