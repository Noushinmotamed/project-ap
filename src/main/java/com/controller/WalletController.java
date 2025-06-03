package com.controller;

import com.DAO.WalletDAO;
import com.google.gson.JsonObject;
import com.models.PaymentMethod;

public class WalletController {
    private final WalletDAO walletDAO;
    public WalletController(){
        walletDAO = new WalletDAO();
    }
    public String topUpWallet(Long id, double amount) {
        if(amount< 0) {
            return "{ \"error\": \"Invalid amount\" }";
        }else{
            return walletDAO.updateWallet(id,amount) ;

        }
    }
    public JsonObject pay(Long orderId, PaymentMethod method, Long userId) {

        return walletDAO.pay(orderId,method,userId);
    }
}
