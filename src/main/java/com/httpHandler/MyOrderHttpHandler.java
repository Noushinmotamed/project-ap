package com.httpHandler;

import com.DAO.MyOrderDAO;
import com.controller.MyOrderController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.httpHandler.AdminHttpHandler.parseQueryParameters;
import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;

public class MyOrderHttpHandler implements HttpHandler {
    private final MyOrderController orderController = new MyOrderController();
    private final MyOrderDAO orderDAO = new MyOrderDAO();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
            return;
        }
        if(path.equals("/orders")){
            if(method.equals("POST")){
                handleSubmitOrder(exchange,Long.parseLong(id));
            }else{
                sendResponse(exchange,405,"{\n  \"error\":\"Method not allowed\"\n}");
                return;
            }
        }else if(path.startsWith("/orders")){
            String[] pathParts = path.split("/");
            if(pathParts.length == 3 && pathParts[1].equals("orders")){
                String orderId = pathParts[2];
                try {
                    if(method.equals("GET")) {
                        handleGetOrder(exchange, Long.parseLong(id), Long.parseLong(orderId));
                    }else{
                        sendResponse(exchange,405,"{\n  \"error\":\"Method not allowed\"\n}");
                        return;
                    }
                }catch(NumberFormatException e){
                    if(pathParts[2].equals("history")){
                        if(method.equals("GET")) {
                            handleGetOrders(exchange, Long.parseLong(id));
                            return;
                        }else{
                            sendResponse(exchange,405,"{\n  \"error\":\"Method not allowed\"\n}");
                            return;
                        }
                    }
                    sendResponse(exchange, 404,"{\n  \"error\":\"Resource not found\"\n}");

                }
            }
        }else {
            sendResponse(exchange, 404,"{\n  \"error\":\"Resource not found\"\n}");
            return;
        }
    }
    private void handleSubmitOrder(HttpExchange exchange, Long id) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject request = new Gson().fromJson(reader, JsonObject.class);
        if(!request.has("delivery_address")){
            sendResponse(exchange,400,"{\n  \"error\":\"Invalid delivery_address\"\n}");
            return;
        }
        if(!request.has("vendor_id")){
            sendResponse(exchange,400,"{\n  \"error\":\"Invalid vendor_id\"\n}");
            return;
        }
        if(!request.has("items")){
            sendResponse(exchange,400,"{\n  \"error\":\"Invalid items\"\n}");
        }
        JsonObject response = orderController.submitOrder(id,request);
        handleResponse(exchange,response);
    }
    private void handleGetOrder(HttpExchange exchange, Long id, Long orderId) throws IOException {
        JsonObject response = orderDAO.getOrder(id,orderId);
        handleResponse(exchange,response);
    }
    private void handleGetOrders(HttpExchange exchange, Long id) throws IOException {
        Map<String, String> queryParams = parseQueryParameters(exchange.getRequestURI().getQuery());
        String search = queryParams.get("search");
        String vendor = queryParams.get("vendor");
        String status = queryParams.get("status");
        JsonElement response = orderController.getOrdersHistory(id,search,vendor,status);
        handleResponse(exchange,response);

    }

}
