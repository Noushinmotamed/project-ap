package com.controller;

import com.DAO.WalletDAO;
import com.google.gson.JsonObject;
import com.models.PaymentMethod;

public class WalletController {
    private final WalletDAO walletDAO;
    public WalletController(){
        walletDAO = new WalletDAO();
    }
    public JsonObject topUpWallet(Long id, double amount) {
        JsonObject response = new JsonObject();
        if(amount< 0) {
            response.addProperty("error", "Invalid amount");
            return response;
        }else{
            return walletDAO.updateWallet(id,amount) ;

        }
    }
    public JsonObject pay(Long orderId, PaymentMethod method, Long userId) {

        return walletDAO.pay(orderId,method,userId);
    }
}
