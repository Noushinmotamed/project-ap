package com.models;

/**
 * restaurantId is null unless one restaurant is added (and only for owner)
 * only one restaurant for one owner
 * email in optional so it could be null
 */
public class User {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
    private long id;
    private long restaurantId;
    private Role role;
}
