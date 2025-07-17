package com.DAO;

import com.google.gson.*;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AdminDAO {
    public JsonElement getUsers(Long id) throws IOException {
              JsonElement response = null;
              Gson gson = new GsonBuilder().setPrettyPrinting().create();
             try(Session session = HibernateUtil.getSessionFactory().openSession()) {
                 Transaction tx = session.beginTransaction();
                 User admin = session.get(User.class, id);
                 if(admin == null){
                     response = new JsonObject();
                     ((JsonObject) response).addProperty("error", "Resource not found");
                     return response;
                 }
                 if(admin.getRole() != Role.admin ) {
                     response = new JsonObject();
                     ((JsonObject) response).addProperty("error", "Forbidden request");
                     return response;
                 }
                 List<User> users = session.createQuery("from User", User.class).list();
                 if( users.isEmpty() ) {
                      response = new JsonObject();
                     ((JsonObject) response).addProperty("error", "Resource not found");
                     return response;
                 }
                 tx.commit();
                 return gson.toJsonTree(users);

             }catch(Exception e){
                 response = new JsonObject();
                 ((JsonObject) response).addProperty("error", "Internal server error");
                 return response;
             }
    }
    public JsonObject updateStatus(Long adminId, Long userId, CourierStatus status) throws IOException {
        JsonObject response = new JsonObject();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User admin = session.get(User.class, adminId);
            if(admin == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(admin.getRole() != Role.admin ) {
                response.addProperty("error", "Forbidden request");
                return response;
            }
            User user = session.get(User.class, userId);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            user.setCourierStatus(status);
            session.update(user);
            tx.commit();
            response.addProperty("message", "Status updated");
            return response;

        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonElement getOrders(Long id,Long itemId, Long vendorId, Long courierId, Long customerId, OrderStatus orderStatus,String name,String vendorName,String courierName,String customerName) {
        JsonElement response = null;
        ArrayList<Long> itemIds = new ArrayList<>();
        ArrayList<Long> vendorIds = new ArrayList<>();
        ArrayList<Long> courierIds = new ArrayList<>();
        ArrayList<Long> customerIds = new ArrayList<>();
        Gson gson = new GsonBuilder().registerTypeAdapter(OrderStatus.class, new OrderStatusSerializer()).setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User admin = session.get(User.class, id);
            if(admin == null){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(admin.getRole() != Role.admin ) {
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            System.out.println(customerName);
            if(customerName != null && !customerName.isEmpty()) {
                List<User> customers = session.createQuery("from User where fullName = :name", User.class).setParameter("name", customerName).getResultList();
                if(!customers.isEmpty()) {
                   for(User user : customers) {
                       customerIds.add(user.getId());
                   }
                }else{
                    System.out.println("did not find customer");
                    response = new JsonObject();
                    ((JsonObject) response).addProperty("error", "Resource not found");
                    return response;
                }
            }
            if(vendorName != null && !vendorName.isEmpty()) {
                List<Restaurant> restaurants = session.createQuery("from Restaurant where name = :name", Restaurant.class).setParameter("name", vendorName).getResultList();
                if(!restaurants.isEmpty()) {
                    for(Restaurant restaurant : restaurants) {
                        vendorIds.add(restaurant.getId());
                    }
                }else{
                    System.out.println("did not find vendor");
                    response = new JsonObject();
                    ((JsonObject) response).addProperty("error", "Resource not found");
                    return response;
                }
            }
            if(courierName != null && !courierName.isEmpty()) {
                List<User> couriers = session.createQuery("from User where fullName = :name", User.class).setParameter("name", courierName).getResultList();
                if(!couriers.isEmpty()) {
                   for(User user : couriers) {
                       courierIds.add(user.getId());
                   }
                }else{
                    System.out.println("did not find courier");
                    response = new JsonObject();
                    ((JsonObject) response).addProperty("error", "Resource not found");
                    return response;
                }
            }
            String hql = "SELECT m FROM Order m WHERE 1=1";
            if( customerId != null ) hql += " AND m.customerId= :customerId ";
            if( vendorId != null ) hql += " AND m.vendorId= :vendorId ";
            if( courierId != null ) hql += " AND m.userId= :courierId ";
            if( orderStatus != null ) hql += " AND m.status= :status ";
            Query<Order> query = session.createQuery(hql , Order.class);

            if(customerId != null) query.setParameter("customerId", customerId);
            if(vendorId != null) query.setParameter("vendorId", vendorId);
            if(courierId != null) query.setParameter("courierId", courierId);
            if( orderStatus != null) query.setParameter("status", orderStatus);
            List<Order> orders = query.getResultList();


            if(orders.isEmpty()){
                System.out.println("No orders found1");
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            // a replacement for iteration on orders that intellij suggested
            if(itemId != null ) {
                System.out.println("itemId: " + itemId);
                orders.removeIf(order -> !order.getItemIds().contains(itemId));
            }else if(name!= null && !name.isEmpty()){
                List<FoodItem> foodItems = session.createQuery("FROM FoodItem WHERE name = :name", FoodItem.class)
                        .setParameter("name", name)
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
                        Collections.disjoint(order.getItemIds(), itemIds)   //remove if order doesn't include any ids
                );
            }
            // Safe removal
            if(!vendorIds.isEmpty())orders.removeIf(order -> !vendorIds.contains(order.getVendorId()));
           if(!customerIds.isEmpty()) orders.removeIf(order -> !customerIds.contains(order.getCustomerId()));
          if(!courierIds.isEmpty()) {
              orders.removeIf(order -> {
                  Long courierId1 = order.getCourierId();
                  return courierId1 == null || (courierIds.stream()
                          .noneMatch(ID -> ID != null && ID.equals(courierId1)));
              });
          }
            if(orders.isEmpty()){
                response = new JsonObject();
                System.out.println("No orders found2");
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            tx.commit();
            return gson.toJsonTree(orders);

        }catch(Exception e){
            response = new JsonObject();
            e.printStackTrace();
            ((JsonObject) response).addProperty("error", "Internal server error");
            return response;
        }


    }
    //search, user, method, status
    public JsonElement getTransactions(Long id, Long customerId,Long orderId, PaymentMethod method, TransactionStatus status,String userName){
           JsonElement response = null;
           List<Long> userIds = new ArrayList<>();
        System.out.println(userName);
           Gson gson = new GsonBuilder().setPrettyPrinting().create();
           try(Session session = HibernateUtil.getSessionFactory().openSession()) {
               Transaction tx = session.beginTransaction();
               User admin = session.get(User.class, id);
               if(admin == null){
                   response = new JsonObject();
                   ((JsonObject) response).addProperty("error", "Resource not found");
                   return response;
               }
               if(admin.getRole() != Role.admin ) {
                   response = new JsonObject();
                   ((JsonObject) response).addProperty("error", "Forbidden request");
                   return response;
               }

               String hql = "SELECT m FROM Transaction m WHERE 1=1";
               if( orderId != null ) hql += " AND m.orderId= :orderId ";
               if( method != null ) hql += " AND m.method= :method ";
               if( status!= null ) hql += " AND m.status= :status ";


               Query<com.models.Transaction> query = session.createQuery(hql , com.models.Transaction.class);
               if( orderId != null ) query.setParameter("orderId", orderId);
               if( method != null ) query.setParameter("method", method);
               if( status != null ) query.setParameter("status", status);

               List<com.models.Transaction> transactions = query.getResultList();
               if(transactions.isEmpty()){
                   response = new JsonObject();
                   ((JsonObject) response).addProperty("error", "Resource not found");
                   return response;
               }
               if(userName != null && !userName.isEmpty()) {
                   List<User> users = session.createQuery("from User where fullName = :name", User.class).setParameter("name", userName).getResultList();
                   if(users.isEmpty()){
                       System.out.println("didn't find user");
                       response = new JsonObject();
                       ((JsonObject) response).addProperty("error", "Resource not found");
                       return response;

                   }
                   for(User user : users){
                       userIds.add(user.getId());
                   }
                   Iterator<com.models.Transaction> iterator = transactions.iterator();
                   while (iterator.hasNext()) {
                       com.models.Transaction transaction = (com.models.Transaction) iterator.next();
                       if (!userIds.contains(transaction.getUserId())) {
                           iterator.remove(); // Safe removal
                       }
                   }
               }
               tx.commit();
               return gson.toJsonTree(transactions);

           } catch (Exception e) {
               response = new JsonObject();
               e.printStackTrace();
               ((JsonObject) response).addProperty("error", "Internal server error");
               return response;
           }
    }

    /**
     * gson class for adjusting OrderStatus enum value ( example : unpaidAndCancelled -> unpaid and cancelled)
     */
    public static class OrderStatusSerializer implements JsonSerializer<OrderStatus> {
        @Override
        public JsonElement serialize(OrderStatus src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getDisplayName());
        }
    }

}
