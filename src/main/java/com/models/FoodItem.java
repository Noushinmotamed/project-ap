package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table( name = "foodItems")
public class FoodItem {
    @Column( nullable = false )
    private String name;

    @Column( nullable = false )
    private String description;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @Column( nullable = false )
    @SerializedName("vendor_id")
    private Long vendorId;

    @Column
    private String imageBase64;

    @Column( nullable = false )
    private int price;

    @Column( nullable = false )
    private int supply;

    @ElementCollection
    @CollectionTable(name = "foodCategories", joinColumns = @JoinColumn(name = "foodId"))
    @Column(name = "categories", nullable = false)
    private List<String> categories= new ArrayList<>();

    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;




    @Column
    private Long menuId;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getFoodId() {
        return foodId;
    }
    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }
    public Long getVendorId() {
        return vendorId;
    }
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }

    public int getSupply() {
        return supply;

    }
    public void setSupply(int supply) {
        this.supply = supply;
    }
    public List<String> getCategories() {
        return categories;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(ArrayList<Rating> ratings) {
        this.ratings = ratings;
    }
    public Long getMenuId() {
        return menuId;
    }
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }





}


