package com.httpHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.models.PaymentMethod;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.controller.WalletController;
import com.util.JwtUtil;

import static com.util.HttpUtil.*;

public class WalletHttpHandler implements HttpHandler {

    private final WalletController walletController;

    public WalletHttpHandler() throws SQLException {
        walletController = new WalletController();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handleWalletTopUp(exchange);
        }else{
            sendResponse(exchange,405,"{ \"error\": \"Method not allowed\" }");
        }
    }

    private void handleWalletTopUp(HttpExchange exchange) throws IOException {
        double amount;
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{ \"error\": \"Unauthorized request\" }");
            return;
        }
        Long userId = Long.valueOf(id);
       try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
           JsonObject requestJson = new Gson().fromJson(reader, JsonObject.class);
            if(!requestJson.has("amount") ) {
                sendResponse(exchange, 400, "{ \"error\": \"Invalid amount\" }");
                return;

            }try {
               amount = Double.parseDouble(requestJson.get("amount").getAsString());
           }catch( NumberFormatException | NullPointerException e ) {
                sendResponse(exchange, 400, "{ \"error\": \"Invalid amount\" }");
                return;
           }
           JsonObject response = walletController.topUpWallet(userId, amount);
            handleResponse(exchange,response);



       } catch (Exception e) {
           sendResponse(exchange, 500, "{ \"error\": \"Internal server error\" }");
       }

    }

    public static void unauthorized(HttpExchange exchange) throws IOException {
      sendResponse(exchange,401,"{\"error\":\"Unauthorized\"}");

    }

}
