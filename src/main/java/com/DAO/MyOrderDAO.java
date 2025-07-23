package com.DAO;

import com.google.gson.*;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.*;

public class MyOrderDAO {
    public JsonObject submitOrder(Long id, Long vendorId, String deliveryAddress, JsonArray itemIdsArray) {
        double rawPrice=0,taxFee=0,additionalFee=0,payPrice=0;
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user==null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, vendorId);
            if(restaurant==null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            taxFee = restaurant.getTax_fee();
            additionalFee = restaurant.getAdditionalFee();
            try {
                for (JsonElement element : itemIdsArray) {
                    int itemId = element.getAsJsonObject().get("item_id").getAsInt();
                    FoodItem item = session.createQuery(
                                    "FROM FoodItem WHERE id = :itemId AND vendorId = :vendorId", FoodItem.class)
                            .setParameter("itemId", itemId)
                            .setParameter("vendorId", vendorId)
                            .uniqueResult();
                    if (item == null) {
                        response.addProperty("error", "Resource not found");
                        System.out.println("item with id " + itemId + " not found");
                        return response;

                    }
                    int quantity = element.getAsJsonObject().get("quantity").getAsInt();
                    rawPrice += item.getPrice()*quantity;
                }
            }catch (Exception e){
                response.addProperty("error", "Invalid items");
                return response;
            }
            Order order = new Order();
            order.setCustomerId(id);
            order.setVendorId(vendorId);
            order.setDeliveryAddress(deliveryAddress);
            Set<Long> itemIds = new HashSet<>();
            for (JsonElement el : itemIdsArray) {
                long ID = el.getAsJsonObject().get("item_id").getAsLong();
                itemIds.add(ID);
            }
            order.setItemIds(itemIds);
            order.setRawPrice((int) rawPrice);
            order.setTaxFee((int) taxFee);
            order.setAdditionalFee((int) additionalFee);
            payPrice = rawPrice + rawPrice *(taxFee/100) + rawPrice * (additionalFee/100);
            System.out.println(payPrice);
            System.out.println(taxFee);
            order.setPayPrice((int) payPrice);
            order.setStatus(OrderStatus.submitted);
            order.setCreatedAt(LocalDateTime.now().toString());
            order.setUserName(user.getFullName());
            // ========================= شروع کد اضافه شده =========================
            try {
                Map<Long, Integer> quantities = new HashMap<>();
                for (JsonElement el : itemIdsArray) {
                    JsonObject itemObject = el.getAsJsonObject();
                    long itemId = itemObject.get("item_id").getAsLong();
                    int quantity = itemObject.get("quantity").getAsInt();
                    quantities.put(itemId, quantity);
                }

                String quantityJsonString = new Gson().toJson(quantities);

                order.setQuantityData(quantityJsonString);

            } catch (Exception e) {
                System.err.println("Warning: Could not save quantity data. " + e.getMessage());
            }
            // ========================== پایان کد اضافه شده ======================
            session.save(order);
            for(JsonElement element : itemIdsArray){
                int itemId = element.getAsJsonObject().get("item_id").getAsInt();
                session.createQuery(
                                "DELETE FROM BasketItem WHERE buyer_id = :buyerId AND food_item_id = :foodItemId")
                        .setParameter("buyerId", id)
                        .setParameter("foodItemId", itemId)
                        .executeUpdate();


            }
            tx.commit();
            return gson.toJsonTree(order).getAsJsonObject();





        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            e.printStackTrace();
            return response;
        }
    }
    public JsonObject getOrder(Long id, Long orderId){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            User user = session.get(User.class, id);
            if(user==null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.buyer){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Order order = session.get(Order.class, orderId);
            if(order==null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!order.getCustomerId().equals(id)){
                response.addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(order).getAsJsonObject();

        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonElement getOrdersHistory(Long id, Long itemId, String itemName, Long vendorId, String vendorName,OrderStatus orderStatus){
        JsonElement response = null;
        ArrayList<Long> itemIds = new ArrayList<>();
        if(itemId!= null) itemIds.add(itemId);
        Gson gson = new GsonBuilder().registerTypeAdapter(OrderStatus.class, new AdminDAO.OrderStatusSerializer()).setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            User buyer = session.get(User.class, id);
            if(buyer==null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                System.out.println("didn't find buyer");
                return response;

            }
            if(buyer.getRole() != Role.buyer){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            if(itemName!= null && !itemName.isEmpty()){
                List<FoodItem> foodItems = session.createQuery("FROM FoodItem WHERE name = :name", FoodItem.class)
                        .setParameter("name", itemName)
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
            }
            if(vendorName!= null && !vendorName.isEmpty()){
                Restaurant restaurant = (Restaurant) session.createQuery("FROM Restaurant  WHERE name = :name").setParameter("name", vendorName)
                        .setMaxResults(1)  // Limit to 1 result
                        .uniqueResult();
                if(restaurant==null){
                    response = new JsonObject();
                    ((JsonObject) response).addProperty("error", "Resource not found");
                    System.out.println("didn't find restaurant");
                    return response;
                }
                vendorId = restaurant.getId();
            }
            String hql = "SELECT m FROM Order m WHERE 1=1";
            hql += " AND m.customerId = :customerId";
            if( vendorId != null ) hql += " AND m.vendorId= :vendorId ";
            if( orderStatus != null ) hql += " AND m.status= :status ";
            Query<Order> query = session.createQuery(hql , Order.class);
            query.setParameter("customerId", id);
            if(vendorId != null) query.setParameter("vendorId", vendorId);
            if( orderStatus != null) query.setParameter("status", orderStatus);

            List<Order> orders = query.getResultList();
            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                System.out.println("didn't find order");
                return response;
            }

            if (!itemIds.isEmpty()) {
                orders.removeIf(order ->
                        Collections.disjoint(order.getItemIds(), itemIds)
                );
            }
            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                System.out.println("didn't find order2");
                return response;
            }
            return gson.toJsonTree(orders);
        } catch (Exception e) {
             response = new JsonObject();
             ((JsonObject) response).addProperty("error", "Internal server error");
             return response;
        }
    }
}
