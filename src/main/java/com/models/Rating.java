package com.models;

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
    private Long ratingId;

    @Column( nullable = false )
    private int rating;

    @Column( nullable = false )
    private String comment;

    @ElementCollection
    @CollectionTable(name = "ratingImages", joinColumns = @JoinColumn(name = "ratingId"))
    @Column(name = "imageBase64")
    private List<String> imageBase64= new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "foodId")
    private FoodItem foodItem;

    @Column( nullable = false )
    private Long itemId;

    @Column
    private String createdAt;
}
