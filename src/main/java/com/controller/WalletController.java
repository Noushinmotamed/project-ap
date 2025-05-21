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
            return "{ \"error\": \"Invalid input\" }";
        }else{
            return walletDAO.updateWallet(id,amount) ;

        }
    }
}
