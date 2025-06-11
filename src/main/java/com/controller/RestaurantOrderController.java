package com.controller;

import com.DAO.RestaurantOrderDAO;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.OrderStatus;

public class RestaurantOrderController {
    private RestaurantOrderDAO restaurantOrderDAO = new RestaurantOrderDAO();
    public JsonElement getOrders(String status,String search,String user,String courier,Long restaurantId,Long id){
        JsonObject response = new JsonObject();
        Long itemId = null, buyerId = null, courierId = null;
        OrderStatus orderStatus = null;
        if(search != null){
            try{
                itemId = Long.parseLong(search);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(user != null){
            try{
                buyerId = Long.parseLong(user);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(courier != null){
            try{
                courierId = Long.parseLong(courier);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(status != null){
            try{
                if(status.equals("unpaid and cancelled")) { status = "unpaidAndCancelled"; }
                else if(status.equals("waiting vendor")) { status = "waitingVendor"; }
                else if(status.equals("finding courier")) { status = "findingCourier"; }
                else if(status.equals("on the way")) { status = "onTheWay"; }
                orderStatus = OrderStatus.valueOf(status);
            }catch(IllegalArgumentException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        return restaurantOrderDAO.getOrders(orderStatus,itemId,buyerId,courierId,restaurantId,id);

    }
}
