package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table( name = "fooditems")
public class FoodItem {
    @Column( nullable = false )
    private String name;

    @Column( nullable = false )
    private String description;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SerializedName("id")
    @Column( name = "foodid")
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "foodcategories",  // PostgreSQL converts to lowercase unless quoted
            joinColumns = @JoinColumn(
                    name = "foodid",  // Must match exact foreign key column name
                    referencedColumnName = "foodid"  // References FoodItem's primary key
            )
    )
    @Column(name = "categories")  // Must match the keyword column name
    private List<String> keywords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "menusList",
            joinColumns = @JoinColumn(name = "foodItem_Id")
    )
    @Column(name = "menu_id")
    private Set<Long> menuId = new HashSet<>();

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getId() {
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
    public List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(List<String> categories) {
        this.keywords = categories;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    public Set<Long> getMenuId(){
        return menuId;
    }
    public void addMenuId(Long menuId){
        this.menuId.add(menuId);
    }
    public void removeMenuId(Long menuId){
        this.menuId.remove(menuId);
    }
    public boolean menuExists(Long menuId){
        return this.menuId.contains(menuId);
    }





}
