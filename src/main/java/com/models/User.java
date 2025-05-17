package com.models;
import jakarta.persistence.*;
import java.util.Objects;
/**
 * restaurantId is null unless one restaurant is added (and only for owner)
 * only one restaurant for one owner
 * email in optional so it could be null
 * id will be created by db
 */
@Entity
@Table(name = "users")
public class User {

    @Column( nullable = true)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column( nullable = true)
    private String email;

    @Column( nullable = false)
    private String password;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String address;

    protected User() {}
    public User(String fullName, String phoneNumber, String email, String password) {
        this.fullName = Objects.requireNonNull(fullName);
        this.phoneNumber = Objects.requireNonNull(phoneNumber);
        this.email = email;
        this.password = Objects.requireNonNull(password);
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }
    public Role getRole() {
        return role;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public Long getRestaurantId() {
        return restaurantId;
    }
    public String getFullName() {
        return fullName;
    }
    public String getAddress() {
        return address;
    }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}

