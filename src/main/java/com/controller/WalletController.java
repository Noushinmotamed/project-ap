package com.controller;

import com.DAO.WalletDAO;

public class WalletController {
    private final WalletDAO walletDAO;
    public WalletController(){
        walletDAO = new WalletDAO();
    }
    public String topUpWallet(Long id, double amount, String method) {
        if(amount< 0 || (!method.equals("card") && !method.equals("online"))) {
            return "{ \"error\": \"Invalid input\" }";
        }else{
            return walletDAO.updateWallet(id,amount) ;

        }
    }
}
