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

import static com.util.HttpUtil.sendResponse;
import static com.util.HttpUtil.sendSimpleResponse;

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
            exchange.sendResponseHeaders(405, 0);
        }
    }

    private void handleWalletTopUp(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{ \"error\": \"unauthorized\" }");
            return;
        }
        Long userId = Long.valueOf(id);
       try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
           JsonObject requestJson = new Gson().fromJson(reader, JsonObject.class);
            if(!requestJson.has("amount") ) {
                sendResponse(exchange, 400, "{ \"error\": \"invalid_input\" }");
                return;
            }
            double amount = requestJson.get("amount").getAsDouble();
            String response = walletController.topUpWallet(userId,amount);
            if(response.contains("error")) {
                if(response.contains("unauthorized")) {
                    sendSimpleResponse(exchange, 401, response);

                }else if(response.contains("invalid_input")) {
                    sendSimpleResponse(exchange, 400, response);

                }else if(response.contains("not_found")) {
                    sendSimpleResponse(exchange, 404, response);

                }else{
                    sendSimpleResponse(exchange, 500, response);
                }
            }else{
                sendSimpleResponse(exchange, 200, response);

            }



       } catch (Exception e) {
           sendResponse(exchange, 400, "{ \"error\": \"invalid_input\" }");
       }

    }

    public static void unauthorized(HttpExchange exchange) throws IOException {
      sendResponse(exchange,401,"{\"error\":\"unauthorized\"}");

    }

}
