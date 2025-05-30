package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

@Entity
@Table( name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SerializedName("menu_id")
    private Long menuId;

    @Column( nullable = false)
    private String title;

    @Column(nullable = false)
    @SerializedName("restaurant_id")
    private Long restaurantId;


    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }


    public Long  getRestaurantId() {
       return restaurantId;
    }
    public Long getMenuId() {
        return menuId;
    }
}
