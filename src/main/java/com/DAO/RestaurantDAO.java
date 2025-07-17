package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;                                 //IF VENDOR IS NOT YOYRS, NOT FOUND OR FORBIDDEN ?
                                                                 // I ASSUME THAT ACCEPTED -> STILL WAITING VENDOR, REJECTED-> CANCELLED, SERVED -> ON THE WAY


public class RestaurantDAO {
    public JsonObject addRestaurant(Restaurant restaurant, Long id){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.seller){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            if(user.getCourierStatus()!= CourierStatus.approved){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant res = session.createQuery("from Restaurant where ownerId = :ownerId", Restaurant.class).setParameter("ownerId", id).uniqueResult();
            if(res!= null){
                response.addProperty("error", "Conflict occurred");
                return response;
            }
            restaurant.setOwnerId(id);
            session.save(restaurant);
            tx.commit();
            return gson.toJsonTree(restaurant).getAsJsonObject();

        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject getRestaurant(Long id){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.seller){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant restaurant = session.createQuery("from Restaurant where ownerId = :ownerId", Restaurant.class).setParameter("ownerId", id).uniqueResult();
            if(restaurant == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(restaurant).getAsJsonObject();

        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject updateRestaurant(JsonObject input, Long id,Long vendorId){
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.seller){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant restaurant = session.get(Restaurant.class, vendorId);
            if(restaurant == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!restaurant.getOwnerId().equals(id)){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            if(input.has("name")) restaurant.setName(input.get("name").getAsString());
            if(input.has("address")) restaurant.setAddress(input.get("address").getAsString());
            if(input.has("phone")) restaurant.setPhone(input.get("phone").getAsString());
            if(input.has("logoBase64")) restaurant.setLogoBase64(input.get("logoBase64").getAsString());
            if(input.has("tax_fee")) restaurant.setTax_fee(input.get("tax_fee").getAsInt());
            if(input.has("additional_fee")) restaurant.setAdditional_fee(input.get("additional_fee").getAsInt());
            session.update(restaurant);
            tx.commit();
            return gson.toJsonTree(restaurant).getAsJsonObject();

        } catch (Exception e) {
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject changeOrderStatus(Long id, Long orderId, OrderVendorStatus status){
        JsonObject response = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(user.getRole() != Role.seller){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            Restaurant restaurant = session.createQuery("from Restaurant where ownerId = :ownerId", Restaurant.class).setParameter("ownerId", id).uniqueResult();
            if(restaurant == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            Order order = session.get(Order.class, orderId);
            if(order == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!order.getVendorId().equals(restaurant.getId())){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            if(order.getVendorStatus()== OrderVendorStatus.served){
                response.addProperty("error", "Conflict occurred");
                return response;
            }
            if(order.getStatus() != OrderStatus.waitingVendor){
                response.addProperty("error", "Forbidden request");
                return response;
            }
            order.setVendorStatus(status);
            switch(status){
                case OrderVendorStatus.accepted:
                    order.setStatus(OrderStatus.waitingVendor);
                    break;
                    case OrderVendorStatus.rejected:
                        order.setStatus(OrderStatus.cancelled);
                        break;
                case OrderVendorStatus.served:
                    order.setStatus(OrderStatus.findingCourier);
                    break;
            }
            session.update(order);
            tx.commit();
            response.addProperty("message", "Order status changed successfully");
            return response;


        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
}
