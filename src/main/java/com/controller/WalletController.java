package com.controller;

import com.DAO.WalletDAO;
import com.models.PaymentMethod;

public class WalletController {
    private final WalletDAO walletDAO;
    public WalletController(){
        walletDAO = new WalletDAO();
    }
    public String topUpWallet(Long id, double amount, PaymentMethod method) {
        if(amount< 0) {
            return "{ \"error\": \"Invalid input or insufficient balance\" }";
        }else{
            return walletDAO.updateWallet(id,amount) ;

        }
    }
    public String pay(Long orderId, double amount, PaymentMethod method, Long userId) {
        if(amount< 0) {
            return "{ \"error\": \"Invalid input or insufficient balance\" }";
        }
        return walletDAO.pay(orderId,amount,method,userId);
    }
}
