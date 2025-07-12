package com.controller;

import com.DAO.MyOrderDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.OrderDeliveryStatus;
import com.models.OrderStatus;

public class MyOrderController {

    private MyOrderDAO orderDAO = new MyOrderDAO();

    public JsonObject submitOrder(Long id, JsonObject request){
        JsonObject response = new JsonObject();
        JsonArray itemIdsArray = new JsonArray();
        Long vendor_id=null;
        String deliveryAddress = request.get("delivery_address").getAsString();
        try {
            JsonArray items = request.getAsJsonArray("items");
            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();
                int itemId = itemObj.get("item_id").getAsInt();
                itemIdsArray.add(itemId);
            }
        } catch (Exception e) {
            response.addProperty("error", "Invalid items");
            return response;
        }
        if(itemIdsArray.isEmpty()){
            response.addProperty("error", "Invalid items");
            return response;
        }

        try{
            vendor_id = Long.parseLong(request.get("vendor_id").getAsString());
        }catch(Exception e){
            response.addProperty("error","Invalid vendor_id");
            return response;
        }
        return orderDAO.submitOrder(id,vendor_id,deliveryAddress,itemIdsArray);

    }
    public JsonElement getOrdersHistory(Long id,String search,String vendor,String status){
        JsonObject response = new JsonObject();
        Long vendorId=null, itemId = null;
        OrderStatus orderStatus= null;
        String itemName=null,vendorName=null;
        if(vendor!= null && !vendor.isEmpty()) {
            try {
                vendorId = Long.parseLong(vendor);
            } catch (NumberFormatException e) {
                vendorName = vendor;
            }
        }
        if(search != null && !search.isEmpty()) {
            try {
                itemId = Long.parseLong(search);
            } catch (NumberFormatException e) {
                itemName = search;
            }
        }
        if(status != null && !status.isEmpty()) {
            try{
                orderStatus = OrderStatus.valueOf(status);
            }catch(IllegalArgumentException e){
                response.addProperty("error","Invalid order status");
                return response;
            }
        }
        return orderDAO.getOrdersHistory(id,itemId,itemName,vendorId,vendorName,orderStatus);

    }
}
