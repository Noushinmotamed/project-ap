package com.controller;
import com.DAO.FoodItemDAO;
import com.models.FoodItem;

public class FoodItemController {
    private FoodItemDAO foodItemDAO = new FoodItemDAO();

    public String addItem(FoodItem foodItem, Long userId, Long restaurantId) {
        if(foodItem.getPrice() < 0)
            return "{\n  \"error\":\"Invalid price\"\n}";
        if(foodItem.getSupply() <= 0)
            return "{\n  \"error\":\"Invalid supply\"\n}";
        if(!foodItem.getVendorId().equals(restaurantId))
            return "{\n  \"error\":\"Invalid vendor\"\n}";

        return foodItemDAO.addItem(foodItem,userId,restaurantId);
    }
}
