package com.controller;
import com.DAO.DeliveryDAO;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.OrderDeliveryStatus;

public class DeliveryController {

    private final DeliveryDAO deliveryDAO = new DeliveryDAO();

    public JsonObject changeOrderStatus(String status, Long orderId,Long id){
        JsonObject response = new JsonObject();
        OrderDeliveryStatus deliveryStatus = null;
        try{
            deliveryStatus = OrderDeliveryStatus.valueOf(status);
        }catch(IllegalArgumentException | NullPointerException e){
            response.addProperty("error", "Invalid status");
            return response;
        }
        return deliveryDAO.changeOrderStatus(deliveryStatus,orderId,id);
    }

    /**
     *
     * @param search  itemId in the list of order (ASSUMED)
     * @param vendor  restaurantId
     * @param user    buyerId
     * @param id      courierId
     * @return        list of  completed or active orders with optional filters
     */

    public JsonElement getOrdersHistory(String search, String vendor, String user, Long id){
        JsonObject response = new JsonObject();
        Long itemId = null, restaurantId = null, buyerId = null;
        if( search != null  ){
            try{
                itemId = Long.parseLong(search);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(vendor != null  ){
            try{
                restaurantId = Long.parseLong(vendor);
            } catch (NumberFormatException e) {
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(user != null  ){
            try{
                buyerId = Long.parseLong(user);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        return deliveryDAO.getOrdersHistory(itemId,restaurantId,buyerId,id);
    }
}
