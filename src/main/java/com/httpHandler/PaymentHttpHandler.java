package com.httpHandler;

import com.controller.WalletController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.models.PaymentMethod;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static com.util.HttpUtil.sendResponse;
import static com.util.HttpUtil.sendSimpleResponse;

public class PaymentHttpHandler implements HttpHandler {
    private WalletController walletController;
    public PaymentHttpHandler() throws SQLException {
        walletController = new WalletController();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePayment(exchange);
        }else{
           exchange.sendResponseHeaders(405, 0);
        }
    }
    private void handlePayment(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendSimpleResponse(exchange, 401, "{ \"error\": \"unauthorized\" }");
            return;
        }
        Long userId = Long.valueOf(id);
        try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)){
            JsonObject requestJson = new Gson().fromJson(reader, JsonObject.class);
            if(!requestJson.has("amount") || !requestJson.has("method") || !requestJson.has("orderId")) {
                sendResponse(exchange, 400, "{ \"error\": \"Invalid input or insufficient balance\" }");
                return;
            }
            Long orderId = requestJson.get("orderId").getAsLong();
            double amount = requestJson.get("amount").getAsDouble();
            PaymentMethod method = PaymentMethod.valueOf(requestJson.get("method").getAsString());
            String response = walletController.pay(orderId, amount, method, userId);
            if(response.contains("error")) {
                if(response.contains("\"error\": \"Unauthorized\"")) {
                    sendResponse(exchange, 401, response);
                }else if(response.contains("Invalid")) {
                    sendResponse(exchange, 400, response);
                }else if(response.contains("not found")) {
                    sendResponse(exchange, 404, response);
                }else if(response.contains("already")){
                    sendResponse(exchange, 400, response);
                }else{
                    sendResponse(exchange, 500, response);
                }

            }else if(response.contains("success")) {
                sendResponse(exchange, 200, response);
            }

        }catch(Exception e){
            sendResponse(exchange, 400, "{ \"error\": \"Invalid input or insufficient balance\" }");
        }
    }
}
