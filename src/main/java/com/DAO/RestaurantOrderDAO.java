package com.DAO;

import com.google.gson.*;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;                          //if you don't own the restaurant forbidden or not found ?
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestaurantOrderDAO {
    public JsonElement getOrders(OrderStatus status,Long itemId,Long buyerId, Long courierId,Long restaurantId, Long sellerId,String name) {
        JsonElement response = null;
        ArrayList<Long> itemIds = new ArrayList<>();
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
            if( buyerId != null ) hql += " AND m.customerId= :customerId ";
            if( courierId != null ) hql += " AND m.courierId= :courierId ";
            if( restaurantId != null ) hql += " AND m.vendorId= :vendorId ";
            if( status != null ) hql += " AND m.status= :status ";
            Query<Order> query = session.createQuery(hql , Order.class);

            if(buyerId != null) query.setParameter("customerId", buyerId);
            if(courierId != null) query.setParameter("courierId", courierId);
            if(restaurantId != null) query.setParameter("vendorId", restaurantId);
            if( status != null) query.setParameter("status", status);
            List<Order> orders = query.getResultList();

            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(itemId != null ) {
                orders.removeIf(order -> !order.getItemIds().contains(itemId));
            }else if(name!= null && !name.isEmpty()){
                List<FoodItem> foodItems = session.createQuery("FROM FoodItem WHERE name = :name AND vendorId = vendorId ", FoodItem.class)
                        .setParameter("name", name)
                        .setParameter("vendorId", restaurantId)
                        .list();
                if(foodItems==null || foodItems.isEmpty()){
                    response = new JsonObject();
                    ((JsonObject) response).addProperty("error", "Resource not found");
                    System.out.println("didn't find item");
                    return response;
                }
                for(FoodItem foodItem : foodItems){
                    itemIds.add(foodItem.getId());
                }

                orders.removeIf(order ->
                        Collections.disjoint(order.getItemIds(), itemIds)
                    );

            }
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
    public JsonObject getOrderDetailsForSeller(Long orderId, Long sellerId) {
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Order order = session.get(Order.class, orderId);
            if (order == null) {
                response.addProperty("error", "Order not found");
                return response;
            }

            Restaurant restaurant = session.createQuery("from Restaurant where ownerId = :sellerId", Restaurant.class)
                    .setParameter("sellerId", sellerId)
                    .uniqueResult();
            if (restaurant == null || !order.getVendorId().equals(restaurant.getId())) {
                response.addProperty("error", "Forbidden request");
                return response;
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject orderJson = gson.toJsonTree(order).getAsJsonObject();
            JsonArray itemsArray = new JsonArray();

            if (order.getItemIds() != null) {
                for (Long itemId : order.getItemIds()) {
                    FoodItem foodItem = session.get(FoodItem.class, itemId);
                    if (foodItem != null) {

                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("foodName", foodItem.getName());
                        itemJson.addProperty("finalPrice", foodItem.getPrice());
                        itemsArray.add(itemJson);


                    }


                }


            }


            orderJson.add("items", itemsArray);





            User customer = session.get(User.class, order.getCustomerId());


            if (customer != null) {


                JsonObject userJson = new JsonObject();


                userJson.addProperty("fullName", customer.getFullName());


                userJson.addProperty("phone", customer.getPhone());


                orderJson.add("user", userJson);


            }





            return orderJson;





        } catch (Exception e) {


            e.printStackTrace();


            response.addProperty("error", "Internal Server Error");


            return response;


        }


    }
}