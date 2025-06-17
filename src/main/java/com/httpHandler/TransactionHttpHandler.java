package com.httpHandler;

import com.DAO.WalletDAO;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;

import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;

public class TransactionHttpHandler implements HttpHandler {
    WalletDAO walletDAO = new WalletDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equals("GET") && exchange.getRequestURI().getPath().equals("/transaction")){
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            String id = JwtUtil.validateToken(token);
            if (id == null) {
                sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
                return;
            }
            getTransactionsHistory(exchange,Long.parseLong(id));
        }else if(!exchange.getRequestURI().getPath().equals("/transaction")){
                     sendResponse(exchange,404,"{\n  \"error\":\"Resource not found\"\n}");

        }else{
            sendResponse(exchange,405,"{\n  \"error\":\"Method not allowed\"\n}");
        }
    }

    private void getTransactionsHistory(HttpExchange exchange,Long id) throws IOException {
        JsonElement response = walletDAO.getTranHistory(id);
        handleResponse(exchange,response);
    }
}
