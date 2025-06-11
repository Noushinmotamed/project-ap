package com.DAO;

import com.google.gson.*;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.lang.reflect.Type;
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
    public JsonElement getOrders(Long id,Long itemId, Long vendorId, Long courierId, Long customerId, OrderStatus orderStatus) {
        JsonElement response = null;
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
            String hql = "SELECT m FROM Order m WHERE 1=1";
            if( customerId != null ) hql += " AND m.userId= :userId ";
            if( vendorId != null ) hql += " AND m.vendorId= :vendorId ";
            if( courierId != null ) hql += " AND m.courierId= :courierId ";
            if( orderStatus != null ) hql += " AND m.status= :status ";
            Query<Order> query = session.createQuery(hql , Order.class);

            if(customerId != null) query.setParameter("userId", customerId);
            if(vendorId != null) query.setParameter("vendorId", vendorId);
            if(courierId != null) query.setParameter("courierId", courierId);
            if( orderStatus != null) query.setParameter("status", orderStatus);
            List<Order> orders = query.getResultList();


            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            // a replacement for iteration on orders that intellij suggested
            if(itemId != null )orders.removeIf(order -> !order.getItemIds().contains(itemId));
            if(orders.isEmpty()){
                response = new JsonObject();
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
    public JsonElement getTransactions(Long id, Long customerId,Long orderId, PaymentMethod method, TransactionStatus status){
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
               String hql = "SELECT m FROM Transaction m WHERE 1=1";
               if( orderId != null ) hql += " AND m.orderId= :orderId ";
               if( method != null ) hql += " AND m.method= :method ";
               if( status!= null ) hql += " AND m.status= :status ";
               if( customerId != null ) hql += " AND m.userId= :userId ";

               Query<Transaction> query = session.createQuery(hql , Transaction.class);
               if( orderId != null ) query.setParameter("orderId", orderId);
               if( method != null ) query.setParameter("method", method);
               if( status != null ) query.setParameter("status", status);
               if (customerId != null ) query.setParameter("userId", customerId);
               List<Transaction> transactions = query.getResultList();
               if(transactions.isEmpty()){
                   response = new JsonObject();
                   ((JsonObject) response).addProperty("error", "Resource not found");
                   return response;
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
