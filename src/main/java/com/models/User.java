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

    @Column( nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column( nullable = true)
    private String email;

    @Column( nullable = false)
    private String password;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long restaurantId;                          //only for owner

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String address;

    @Column
    private String profileImageBase64;

    @Embedded
    private BankInfo bankInfo;

    @Column
    private String brandName;                      //only for owner
     @Column
    private String explanation;                     //only for owner
    @Column
    private CourierStatus courierStatus;             // only for courier

    protected User() {}
    public User(String fullName, String phone, String email, String password,String address, String profileImageBase64, Role role, BankInfo bankInfo) {
        this.fullName = Objects.requireNonNull(fullName);
        this.phone = Objects.requireNonNull(phone);
        this.email = email;
        this.password = Objects.requireNonNull(password);
        this.address = Objects.requireNonNull(address);
        this.profileImageBase64 = profileImageBase64;
        this.role = Objects.requireNonNull(role);
        this.bankInfo = bankInfo;

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
    public String getPhone() {
        return phone;
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
    public void setPhone(String phoneNumber) { this.phone = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public String getImageBase64() { return profileImageBase64; }
    public BankInfo getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfo bankInfo) { this.bankInfo = bankInfo; }
    public void setImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

}

