package com.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String text;

    private String imageUrl;

    @Column(nullable = false)
    private long userid;

    @Column(nullable = false)
    private long foodid;

    // Getters
    public Long getReviewId() {
        return reviewId;
    }

    public int getRating() {
        return rating;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getUserid() {
        return userid;
    }

    public long getFoodId() {
        return foodid;
    }

    // Setters
    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUser(long userid) {
        this.userid = userid;
    }

    public void setFood(long foodid) {
        this.foodid = foodid;
    }
}
