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

import static com.util.HttpUtil.*;


public class PaymentHttpHandler implements HttpHandler {
    private final WalletController walletController;
    public PaymentHttpHandler()  {
        walletController = new WalletController();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePayment(exchange);
        }else{
          sendResponse(exchange,405,"{\"error\":\"Method not allowed\"}");
        }
    }
    private void handlePayment(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{ \"error\": \"Unauthorized request\" }");
            return;
        }
        Long userId = Long.valueOf(id);
        try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)){
            JsonObject requestJson = new Gson().fromJson(reader, JsonObject.class);
            PaymentMethod method = null;
            Long orderId = null;
            if( !requestJson.has("method")) {
                sendResponse(exchange, 400, "{ \"error\": \"Invalid method\" }");
                return;
            }else{
                try{
                    method = PaymentMethod.valueOf(requestJson.get("method").getAsString());
                }catch(Exception e){
                    sendResponse(exchange, 400, "{ \"error\": \"Invalid method\" }");
                    return;
                }

            }
            if( !requestJson.has("order_id")) {
                sendResponse(exchange, 400, "{ \"error\": \"Invalid order_id\" }");
                return;
            }else{
                try{
                    orderId = Long.parseLong(requestJson.get("order_id").getAsString());
                }catch(NumberFormatException | NullPointerException e){
                    sendResponse(exchange, 400, "{ \"error\": \"Invalid order_id\" }");
                    return;
                }
            }
            JsonObject response = walletController.pay(orderId,method, userId);
            handleResponse(exchange,response);

        }catch(Exception e){
            sendResponse(exchange, 500, "{ \"error\": \"Internal server error\" }");
        }
    }
}
