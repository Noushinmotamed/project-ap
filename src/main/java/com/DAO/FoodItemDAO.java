package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.models.FoodItem;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;


public class FoodItemDAO {


    public JsonObject addItem(FoodItem foodItem, Long userId, Long restaurantId) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject response = new JsonObject();
        try( Session session = HibernateUtil.getSessionFactory().openSession() ){

            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class,userId);
            if(seller == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class,restaurantId);
            if(restaurant == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!restaurant.getOwnerId().equals(seller.getId())){
               response.addProperty("error", "Forbidden request");
               return response;
            }
            session.save(foodItem);
            tx.commit();
            return gson.toJsonTree(foodItem).getAsJsonObject();

        } catch (Exception e) {
           response.addProperty("error", "Internal server error");
           return response;
        }
    }
    public JsonObject editItem(FoodItem foodItem,Long userId,Long restaurantId,Long itemId) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject response = new JsonObject();
        try( Session session = HibernateUtil.getSessionFactory().openSession() ){

            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class,userId);

            if(seller == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class,restaurantId);
            if(restaurant == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!restaurant.getOwnerId().equals(seller.getId())){
               response.addProperty("error", "Forbidden request");
               return response;
            }
            FoodItem foodItemToEdit = session.get(FoodItem.class,itemId);
            System.out.println("keywords :" + foodItem.getKeywords());
            if(foodItemToEdit == null){
               response.addProperty("error", "Resource not found");
               return response;
            }
            if(!foodItemToEdit.getVendorId().equals(restaurantId)) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            if(foodItem.getName()!= null){
                foodItemToEdit.setName(foodItem.getName());
            }
            if(foodItem.getDescription()!= null){
                foodItemToEdit.setDescription(foodItem.getDescription());
            }
            if(foodItem.getImageBase64() != null){
                foodItemToEdit.setImageBase64(foodItem.getImageBase64());
            }
            if(foodItem.getSupply() > 0){
                foodItemToEdit.setSupply(foodItem.getSupply());
            }
            if(foodItem.getPrice() > 0){
                foodItemToEdit.setPrice(foodItem.getPrice());
            }

            if (foodItem.getKeywords() != null && !foodItem.getKeywords().isEmpty()) {
                foodItemToEdit.setKeywords(foodItem.getKeywords());
            } else {
                foodItemToEdit.setKeywords(foodItemToEdit.getKeywords()); // Retain previous value
            }


            Hibernate.initialize(foodItemToEdit.getKeywords());


            session.merge(foodItemToEdit);


            session.update(foodItemToEdit);
            tx.commit();
            return gson.toJsonTree(foodItemToEdit).getAsJsonObject();




        }catch(Exception e){
        e.printStackTrace();
        response.addProperty("error", "Internal server error");
        return response;

        }
    }
    public JsonObject deleteItem(Long userId, Long restaurantId, Long itemId) {
        JsonObject response = new JsonObject();
        System.out.println("restaurant :" +restaurantId);
        System.out.println("user :" + userId);
        System.out.println("item :" + itemId);
        try( Session session = HibernateUtil.getSessionFactory().openSession() ){
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class,userId);
            if(seller == null){
                response.addProperty("error", "Resource not found");
                System.out.println("seller is null");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class,restaurantId);
            if(restaurant == null){
                response.addProperty("error", "Resource not found");
                System.out.println("restaurant is null");
                return response;
            }
            if(!restaurant.getOwnerId().equals(seller.getId())){
                response.addProperty("error", "Forbidden request");
                System.out.println("forbidden request");
                return response;
            }
            FoodItem foodItem = session.get(FoodItem.class,itemId);
            if(foodItem == null){
                response.addProperty("error", "Resource not found");
                System.out.println("resource not found");
                return response;
            }
            if(!foodItem.getVendorId().equals(restaurantId)) {
                response.addProperty("error", "Forbidden request");
                System.out.println("forbidden request");
                return response;
            }
            session.delete(foodItem);
            tx.commit();
            response.addProperty("message", "Food item removed successfully");
            return response;
        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
    }

    }
}
