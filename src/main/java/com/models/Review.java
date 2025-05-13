package com.models;

/**
 * directly connected to User class and Food class for displaying sake
 */
public class Review {
    private long reviewId;
    private int rating;
    private String text;
    private String imageUrl;
    private User user;
    private Food food;
}
