package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * directly connected to User class and FoodItem class for displaying sake
 */
@Entity
@Table( name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SerializedName("id")
    private Long id;

    @Column
    private int rating;

    @Column
    @SerializedName("user_id")
    private Long userId;

    @Column
    private String comment;

    @Column
    @SerializedName("name")
    private String userName;

    @ElementCollection
    @CollectionTable(name = "ratingImages", joinColumns = @JoinColumn(name = "ratingId"))
    @Column(name = "imageBase64")
    private List<String> imageBase64= new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "rating_food_items", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "food_item_id")
    private List<Long> foodItemIds = new ArrayList<>();


    @Column
    @SerializedName("item_id")   //this is order id !
    private Long itemId;

    @Column
    @SerializedName("created_at")
    private String createdAt;

    public void setRating(int rating) {
        if(rating < 1) rating = 1;
        if(rating > 5) rating = 5;
        this.rating = rating;
    }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setComment(String comment) { this.comment = comment; }
    public void setImageBase64(List<String> imageBase64) { this.imageBase64 = imageBase64; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setFoodItemIds(List<Long> foodItemIds) { this.foodItemIds = foodItemIds; }
    public long getOrderId() { return itemId; }
    public int getRating(){ return rating;  }
    public String getComment(){ return comment; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public List<String> getImageBase64() { return imageBase64; }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserName() { return userName; }
}
