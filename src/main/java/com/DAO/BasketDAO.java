package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class BasketDAO {
    public JsonElement getBasket(Long id) {
        JsonElement response = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if(user == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            String hql = "SELECT m FROM BasketItem m WHERE m.buyer_id= : id";
            Query<BasketItem> query = session.createQuery(hql , BasketItem.class);
            query.setParameter("id", id);
            List<BasketItem> items = query.getResultList();
            if(items.isEmpty()) {
                response = new JsonObject();

            }

            return gson.toJsonTree(items);

        } catch (Exception e) {
            response = new JsonObject();
            ((JsonObject) response).addProperty("error","Internal server error");
            return response;
        }

    }
    public JsonElement getBasket(Long id, Long vendorId) {
        JsonElement response = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if(user == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            String hql = "SELECT m FROM BasketItem m WHERE m.buyer_id= : id AND m.vendorId= : vendorId";
            Query<BasketItem> query = session.createQuery(hql , BasketItem.class);
            query.setParameter("id", id);
            query.setParameter("vendorId", vendorId);
            List<BasketItem> items = query.getResultList();
            if(items.isEmpty()) {
                response = new JsonObject();

            }

            return gson.toJsonTree(items);

        } catch (Exception e) {
            response = new JsonObject();
            ((JsonObject) response).addProperty("error","Internal server error");
            return response;
        }
    }
    public JsonObject addToBasket(Long id, Long foodId, BasketItem basketItem) {
        JsonObject response = new JsonObject();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            basketItem.setBuyer_id(id);
            FoodItem foodItem = session.get(FoodItem.class, foodId);
            if(foodItem == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            basketItem.setFood_item_id(foodId);
            basketItem.setVendorId(foodItem.getVendorId());
            basketItem.setName(foodItem.getName());
            basketItem.setPrice(foodItem.getPrice());
            session.save(basketItem);
            tx.commit();
            response.addProperty("message", "Item added successfully");
            return response;

        }

    }
    public JsonObject removeFromBasket(Long id, Long basketItemId){
      JsonObject response = new JsonObject();
      try(Session session = HibernateUtil.getSessionFactory().openSession()) {
          Transaction tx = session.beginTransaction();
          User user = session.get(User.class, id);
          if(user == null){
              response.addProperty("error", "Resource not found");
              return response;
          }
          BasketItem basketItem = session.get(BasketItem.class, basketItemId);
          session.delete(basketItem);
          tx.commit();
          response.addProperty("message", "Item removed successfully");
          return response;
      }catch(Exception e){
          response.addProperty("error","Internal server error");
          return response;
      }
    }
}
