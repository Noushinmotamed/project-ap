package com.controller;

import com.DAO.WalletDAO;
import com.models.PaymentMethod;

public class WalletController {
    private final WalletDAO walletDAO;
    public WalletController(){
        walletDAO = new WalletDAO();
    }
    public String topUpWallet(Long id, double amount) {
        if(amount< 0) {
            return "{ \"error\": \"invalid_input\" }";
        }else{
            return walletDAO.updateWallet(id,amount) ;

        }
    }
    public String pay(Long orderId, double amount, PaymentMethod method, Long userId) {
        if(amount< 0) {
            return "{ \"error\": \"invalid_input\" }";
        }
        return walletDAO.pay(orderId,amount,method,userId);
    }
}
