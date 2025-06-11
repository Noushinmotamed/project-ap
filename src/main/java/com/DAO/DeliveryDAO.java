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

import java.time.LocalDateTime;
import java.util.List;                                                       //I ASSUMED IF DELIVERY HASN'T YET REACHED THE FINDING COURIER STAGE IT'S FORBIDDEN

public class DeliveryDAO {

    public JsonElement getOrders(String id) {
        JsonElement response = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(OrderStatus.class, new AdminDAO.OrderStatusSerializer()).setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            User courier =session.get(User.class, id);
            if( courier == null ) {
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(courier.getRole() != Role.courier || courier.getCourierStatus() != CourierStatus.approved){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            String hql = "FROM Order WHERE status = :status";
            OrderStatus status = OrderStatus.findingCourier;
            List<Order> orders = session.createQuery(hql, Order.class)
                    .setParameter("status", status)
                    .getResultList();
            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(orders);



        }
    }
    public JsonObject changeOrderStatus(OrderDeliveryStatus status, Long orderId, Long id){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().registerTypeAdapter(OrderStatus.class, new AdminDAO.OrderStatusSerializer()).setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User courier =session.get(User.class, id);
            if( courier == null ) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(courier.getRole() != Role.courier || courier.getCourierStatus() != CourierStatus.approved){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Order order = session.get(Order.class, orderId);
            if( order == null ) {
                response.addProperty("error", "Resource not found");
                return response;
            }
            if( order.getStatus() != OrderStatus.findingCourier){
                if(order.getDeliveryStatus() != null){
                    response.addProperty("error", "Delivery already assigned");
                    return response;
                }else{
                    response.addProperty("error", "Forbidden request");
                    return response;
                }
            }
            switch(status){
                case accepted ->
                    order.setStatus(OrderStatus.onTheWay);
                case delivered, received ->
                    order.setStatus(OrderStatus.complete);
            }
            order.setDeliveryStatus(status);
            order.setUpdatedAt(LocalDateTime.now().toString());
            order.setCourierId(id);
            session.update(order);
            tx.commit();
            response.addProperty("message", "Changed status successfully");
            JsonElement orderJson = gson.toJsonTree(order);
            response.add("order", orderJson);
            return response;

        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }

    }
    public JsonElement getOrdersHistory(Long itemId,Long restaurantId,Long buyerId,Long id){
        JsonElement response;
        Gson gson = new GsonBuilder().registerTypeAdapter(OrderStatus.class, new AdminDAO.OrderStatusSerializer()).setPrettyPrinting().create();
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User courier =session.get(User.class, id);
            if( courier == null ) {
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(courier.getRole() != Role.courier || courier.getCourierStatus() != CourierStatus.approved){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Forbidden request");
                return response;
            }
            String hql = "SELECT m FROM Order m WHERE 1=1";
            if( buyerId != null ) hql += " AND m.userId= :userId ";
            if( restaurantId != null ) hql += " AND m.vendorId= :vendorId ";
            if( id != null ) hql += " AND m.courierId= :courierId ";

            Query<Order> query = session.createQuery(hql , Order.class);

            if(buyerId != null) query.setParameter("userId", buyerId);
            if(restaurantId != null) query.setParameter("vendorId", restaurantId);
            if(id != null) query.setParameter("courierId", id);

            List<Order> orders = query.getResultList();
            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            if(itemId != null )orders.removeIf(order -> !order.getItemIds().contains(itemId));
            if(orders.isEmpty()){
                response = new JsonObject();
                ((JsonObject) response).addProperty("error", "Resource not found");
                return response;
            }
            tx.commit();
            return gson.toJsonTree(orders);

        } catch (Exception e) {
            response = new JsonObject();
            ((JsonObject) response).addProperty("error", "Internal server error");
            return response;
        }
    }
}
