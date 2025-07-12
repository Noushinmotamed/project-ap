package com.controller;

import com.DAO.RatingDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.models.Rating;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RatingController {

    private final RatingDAO ratingDAO = new RatingDAO();

    public JsonObject addRating(JsonObject input, Long userId) {
        Rating rating = new Rating();
        JsonObject error = new JsonObject();
        rating.setUserId(userId);
        if(input.get("comment").getAsString().isEmpty()){
            error.addProperty("error", "Invalid comment");
            return error;
        }
        rating.setComment(input.get("comment").getAsString());
        try{
            int ratingStars = Integer.parseInt(input.get("rating").getAsString());
            if(ratingStars < 1 || ratingStars > 5){
                error.addProperty("error", "Invalid rating");
                return error;
            }
            rating.setRating(ratingStars);
        } catch (NumberFormatException e) {
            error.addProperty("error", "Invalid rating");
            return error;
        }
        try{
            rating.setItemId((long) Integer.parseInt(input.get("order_id").getAsString()));
        }catch (NumberFormatException e){
            error.addProperty("error", "Invalid order_id");
            return error;
        }
        if(input.has("imageBase64"))try{
            JsonArray imageArray = input.getAsJsonArray("imageBase64");
            List<String> base64List = new ArrayList<>();
            for (int i = 0; i < imageArray.size(); i++) {
                base64List.add(imageArray.get(i).getAsString());
            }
            rating.setImageBase64(base64List);

        }catch (Exception e){
            error.addProperty("error", "Invalid imageBase64");
            return error;
        }
        rating.setCreatedAt(LocalDateTime.now().toString());
        return ratingDAO.addRating(rating,userId);
    }
    public JsonObject editRating(JsonObject input, Long userId, Long ratingId) {
       JsonObject error = new JsonObject();
       List<String> imageBase64List= null ;
       String comment= null;
       Integer rating= null;
       if(input.has("comment")){
             comment = input.get("comment").getAsString();
       }
       if(input.has("rating")){
           try{
               rating = Integer.parseInt(input.get("rating").getAsString());
               if(rating < 1 || rating > 5){
                   error.addProperty("error", "Invalid rating");
                   return error;
               }
           }catch (NumberFormatException e){
               error.addProperty("error", "Invalid rating");
               return error;
           }
       }
       if(input.has("imageBase64")){
           try {
                imageBase64List = new Gson().fromJson(
                       input.get("imageBase64"),
                       new TypeToken<List<String>>() {
                       }.getType()
               );
           }catch (Exception e){
               error.addProperty("error", "Invalid imageBase64");
               return error;
           }
       }
       return ratingDAO.editRating(userId,ratingId,rating,comment,imageBase64List);
    }
}
