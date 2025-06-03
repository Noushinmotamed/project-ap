package com.controller;

import com.DAO.AdminDAO;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.OrderStatus;
import com.models.PaymentMethod;
import com.models.TransactionStatus;



/**
 *  search -> itemIds in order
 *  search -> orderId in transaction
 */

public class AdminController {
    private AdminDAO adminDAO = new AdminDAO();
    public JsonElement getOrders(Long id, String search, String vendor, String courier, String customer, String status) {
        Long itemId = null,vendorId = null,courierId= null,customerId= null;
        JsonObject response = new JsonObject();
        OrderStatus orderStatus = null;
        if( search != null){
            try{
                itemId = Long.parseLong(search);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(vendor!= null){
            try{
                vendorId = Long.parseLong(vendor);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(courier!= null){
            try{
                courierId = Long.parseLong(courier);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(customer!= null){
            try {
                customerId = Long.parseLong(customer);
            }catch(NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(status != null) {
            try{

                if(status.equals("unpaid and cancelled")) { status = "unpaidAndCancelled"; }
                else if(status.equals("waiting vendor")) { status = "waitingVendor"; }
                else if(status.equals("finding courier")) { status = "findingCourier"; }
                else if(status.equals("on the way")) { status = "onTheWay"; }
                orderStatus = OrderStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        return adminDAO.getOrders(id,itemId,vendorId,courierId,customerId,orderStatus);


    }
    public JsonElement getTransactions(Long id, String search, String user, String method, String status) {
        Long orderId = null,customerId = null;
        PaymentMethod paymentMethod = null;
        TransactionStatus transactionStatus = null;
        JsonObject response = new JsonObject();
        if(search != null){
            try{
                orderId = Long.parseLong(search);
            }catch (NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(user != null){
            try{
                customerId = Long.parseLong(user);
            }catch (NumberFormatException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(method != null){
            try{
                paymentMethod = PaymentMethod.valueOf(method);
            }catch (IllegalArgumentException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        if(status != null) {
            try{
                transactionStatus = TransactionStatus.valueOf(status);
            }catch (IllegalArgumentException e){
                response.addProperty("error", "Resource not found");
                return response;
            }
        }
        return adminDAO.getTransactions(id,customerId,orderId,paymentMethod,transactionStatus);
    }
}
