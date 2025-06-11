package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;                          //if you don't own the restaurant forbidden or not found ?
import org.hibernate.query.Query;

import java.util.List;

public class RestaurantOrderDAO {
    public JsonElement getOrders(OrderStatus status,Long itemId,Long buyerId, Long courierId,Long restaurantId, Long sellerId) {
        JsonElement response = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(OrderStatus.class, new AdminDAO.OrderStatusSerializer()).setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User seller = session.get(User.class, sellerId);
            if(seller == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(seller.getRole() != Role.seller){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if(restaurant == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(!restaurant.getOwnerId().equals(sellerId)){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            String hql = "SELECT m FROM Order m WHERE 1=1";
            if( buyerId != null ) hql += " AND m.userId= :userId ";
            if( courierId != null ) hql += " AND m.courierId= :courierId ";
            if( restaurantId != null ) hql += " AND m.vendorId= :vendorId ";
            if( status != null ) hql += " AND m.status= :status ";
            Query<Order> query = session.createQuery(hql , Order.class);

            if(buyerId != null) query.setParameter("userId", buyerId);
            if(courierId != null) query.setParameter("courierId", courierId);
            if(restaurantId != null) query.setParameter("vendorId", restaurantId);
            if( status != null) query.setParameter("status", status);
            List<Order> orders = query.getResultList();

            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(itemId != null )orders.removeIf(order -> !order.getItemIds().contains(itemId));                  //delete if doesn't include the item
            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                System.out.println("order not found");
                return response;
            }
            tx.commit();
            return gson.toJsonTree(orders);


        } catch (Exception e) {
            response = new JsonObject();
            ((JsonObject) response).addProperty("error", "Internal Server Error");
            return response;
        }
    }
}