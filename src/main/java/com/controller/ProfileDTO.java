package com.controller;

public class ProfileDTO {
    private String fullName;
    private String phoneNumber;
    private String address;
    private String imageUrl;
    private String email;
    public ProfileDTO(String fullName, String phoneNumber, String address, String imageUrl, String email) {
        this.fullName =  fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.imageUrl =  imageUrl;
        this.email =  email;

    }
    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getAddress() {
        return address;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) { this.email = email; }


}
