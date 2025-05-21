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
            sendResponse(exchange, 401, "{ \"error\": \"Unauthorized\" }");
            return;
        }
        Long userId = Long.valueOf(id);
       try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
           JsonObject requestJson = new Gson().fromJson(reader, JsonObject.class);
            if(!requestJson.has("amount") || !requestJson.has("method")) {
                sendResponse(exchange, 400, "{ \"error\": \"Invalid input\" }");
                return;
            }
            String method = requestJson.get("method").getAsString();
            double amount = requestJson.get("amount").getAsDouble();
            PaymentMethod paymentMethod = PaymentMethod.valueOf(method);
            String response = walletController.topUpWallet(userId,amount,paymentMethod);
            if(response.contains("error")) {
                if(response.contains("Unauthorized")) {
                    sendResponse(exchange, 401, response);

                }else if(response.contains("Invalid input")) {
                    sendResponse(exchange, 400, response);

                }else if(response.contains("Not Found")) {
                    sendResponse(exchange, 404, response);

                }else{
                    sendResponse(exchange, 500, response);
                }
            }else{
                sendResponse(exchange, 200, response);

            }



       } catch (Exception e) {
           sendResponse(exchange, 400, "{ \"error\": \"Invalid input\" }");
       }

    }

    public static void unauthorized(HttpExchange exchange) throws IOException {
      sendResponse(exchange,401,"{\"error\":\"Unauthorized\"}");

    }

}
