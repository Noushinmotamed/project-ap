package com.DAO;

import com.google.gson.*;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;                                              // I RETURN 404 IF THERE ARE NO RATINGS FOR AN ITEM, SHOULD IT BE EMPTY ARRAY ?

public class RatingDAO {
    public JsonObject addRating(Rating rating, Long userId) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Order order = session.get(Order.class, rating.getOrderId());
            if(order == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!order.getCustomerId().equals(userId)){
                response.addProperty("error", "Resource not found");
                return response;
            }
//            if(order.getStatus() != OrderStatus.complete){
//                response.addProperty("error", "Forbidden request");
//                return response;
//            }
            Rating rating1 = session.createQuery(
                            "FROM Rating WHERE itemId = :itemId", Rating.class)
                    .setParameter("itemId", rating.getOrderId())
                    .uniqueResult();
            if(rating1 != null){
                response.addProperty("error", "Conflict occurred");
                return response;
            }
            // Convert Set to List directly
            rating.setFoodItemIds(new ArrayList<>(order.getItemIds()));
            rating.setUserName(user.getFullName());
            session.save(rating);
            tx.commit();
            return gson.toJsonTree(rating).getAsJsonObject();

        } catch (Exception e) {
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject getRatings(Long itemId, Long userId) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
             response.addProperty("error", "Resource not found");
             return response;
            }
            if(user.getRole() != Role.buyer){
               response.addProperty("error", "Forbidden request");
               return response;
            }
            FoodItem foodItem = session.get(FoodItem.class, itemId);
            if(foodItem == null){
               response.addProperty("error", "Resource not found");
               return response;
            }
            List<Rating> ratings = session.createQuery(
                            "SELECT DISTINCT r FROM Rating r JOIN r.foodItemIds f WHERE f = :foodItemId",
                            Rating.class)
                    .setParameter("foodItemId", itemId)
                    .getResultList();
            if(ratings.isEmpty()){
              response.addProperty("error", "Resource not found");
              return response;
            }
            Double averageRating = calculateAverage(ratings);
            response.addProperty("avg_rating", averageRating);
            JsonArray ratingsArray = new JsonArray();
            for (Rating rating : ratings) {
                JsonObject ratingJson = new JsonObject();
                ratingJson.addProperty("id", rating.getId());
                ratingJson.addProperty("name", rating.getUserName());
                ratingJson.addProperty("item_id", rating.getOrderId());
                ratingJson.addProperty("rating", rating.getRating());
                ratingJson.addProperty("comment", rating.getComment());
                ratingJson.addProperty("user_id", rating.getUserId());
                ratingJson.addProperty("created_at", rating.getCreatedAt());
                ratingJson.add("imageBase64", gson.toJsonTree(rating.getImageBase64()));
                ratingsArray.add(ratingJson);
            }

            response.add("comments", ratingsArray);
            tx.commit();
            return response;



        }catch(Exception e){
            response = new JsonObject();
            ((JsonObject) response).addProperty("error", "Internal server error");
            return response;
        }
    }
    private double calculateAverage(List<Rating> ratings){
        double sum = 0.0;
        for(Rating rating : ratings){
            sum += rating.getRating();
        }
        return sum / ratings.size();

    }
    public JsonObject getRating(Long userId, Long ratingId) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            User user = session.get(User.class, userId);
            if (user == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Rating rating = session.get(Rating.class, ratingId);
            if(rating == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(rating).getAsJsonObject();

        }
    }
    public JsonObject editRating(Long userId, Long ratingId,  int ratingStars, String comment, List<String> imageBase64List) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Rating rating = session.get(Rating.class, ratingId);
            if(rating == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!rating.getUserId().equals(userId)){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            rating.setRating(ratingStars);
            rating.setComment(comment);
            rating.setImageBase64(imageBase64List);
            session.update(rating);
            tx.commit();
            return gson.toJsonTree(rating).getAsJsonObject();

        }
    }
    public JsonObject deleteRating(Long userId, Long ratingId) {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Rating rating = session.get(Rating.class, ratingId);
            if(rating == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!rating.getUserId().equals(userId)){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            session.delete(rating);
            tx.commit();
            response.addProperty("message", "Rating deleted");
            return response;
        }
    }
    public JsonObject getRatingByOrder(Long userId, Long orderId) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            User user = session.get(User.class, userId);
            if (user == null) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Rating rating = session.createQuery(
                            "FROM Rating r WHERE r.itemId = :orderId AND r.userId = :userId", Rating.class)
                    .setParameter("orderId", orderId)
                    .setParameter("userId", userId)
                    .uniqueResult();
            if(rating == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            response.add("rating", gson.toJsonTree(rating));
            return response;

        } catch (Exception e) {
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
}
