package com.controller;

public class OwnerProfileDTO extends ProfileDTO {
    private String bankInfo;
    private String brandName;
    private String explanation;
    public OwnerProfileDTO(String fullName, String phoneNumber, String address, String imageUrl, String email, String bankInfo, String brandName, String explanation) {
        super(fullName,phoneNumber,address,imageUrl,email);
        this.bankInfo = bankInfo;
        this.brandName = brandName;
        this.explanation = explanation;

    }
    public String getBankInfo() {
        return bankInfo;
    }
   public String getBrandName() {
    return brandName;}
    public String getExplanation() {
        return explanation;
    }
}

