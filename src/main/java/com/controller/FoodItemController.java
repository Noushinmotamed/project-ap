package com.controller;
import com.DAO.FoodItemDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.models.FoodItem;

/**
 * price cannot be set to zero
 */
public class FoodItemController {
    private FoodItemDAO foodItemDAO = new FoodItemDAO();

    public JsonObject addItem(FoodItem foodItem, Long userId, Long restaurantId) {
        JsonObject response = new JsonObject();

        if(foodItem.getPrice() < 0) {
            response.addProperty("error", "Invalid price");
            return response;
        }
        if(foodItem.getSupply() <= 0) {
            response.addProperty("error", "Invalid supply");
            return response;
        }
        if(!foodItem.getVendorId().equals(userId)) {
            response.addProperty("error", "Invalid vendor_id");
            return response;
        }

        return foodItemDAO.addItem(foodItem,userId,restaurantId);
    }
    public JsonObject editItem(FoodItem foodItem, Long userId, Long restaurantId, Long itemId) {
        JsonObject response = new JsonObject();
        if(foodItem.getPrice()< 0 ) {
            response.addProperty("error", "Invalid price");
            return response;
        }
        if(foodItem.getSupply() < 0){
            response.addProperty("error", "Invalid supply");
            return response;
        }

        return foodItemDAO.editItem(foodItem,userId,restaurantId,itemId);
    }
    public JsonObject deleteItem( Long userId, Long restaurantId, Long itemId) {
        return foodItemDAO.deleteItem(userId,restaurantId,itemId);
    }
}
