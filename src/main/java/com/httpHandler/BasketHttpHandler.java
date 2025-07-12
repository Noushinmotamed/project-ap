package com.httpHandler;

import com.DAO.BasketDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.models.Basket;
import com.models.BasketItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;

/**
 * for client
 * path : /basket  (get)
 * path : /basket/{foodId}  (post)
 * json body : quantity
 *  /basket/{basketItemId}                (delete)
 */
public class BasketHttpHandler implements HttpHandler {
    private BasketDAO basketDAO = new BasketDAO();
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
        if (path.equals("/basket")) {
            if(method.equals("GET")) {
                handleGetBasket(exchange,Long.parseLong(id));
            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
            }
        }else if(path.startsWith("/basket")) {
            String[] pathParts = path.split("/");
            if(pathParts.length == 3) {
                if(pathParts[1].equals("basket")) {
                    String foodId = pathParts[2];
                    if(method.equals("POST")) {
                        handleAddToBasket(exchange, Long.parseLong(id), Long.parseLong(foodId));
                    }else if(method.equals("DELETE")) {
                        handleRemoveFromBasket(exchange,Long.parseLong(id),Long.parseLong(foodId));
                    }else if(method.equals("GET")) {
                        String vendorId = pathParts[2];
                        handleGetBasketByVendor(exchange,Long.parseLong(id),Long.parseLong(vendorId));
                    }
                }
            }else{
                sendResponse(exchange, 404, "{\n  \"Resource not found\"\n}");
            }
        }

    }
    private void handleGetBasket(HttpExchange exchange, Long id) throws IOException {
        JsonElement response =basketDAO.getBasket(id);
        handleResponse(exchange,response);

    }
    private void handleGetBasketByVendor(HttpExchange exchange, Long id,Long vendorId) throws IOException {
        JsonElement response = basketDAO.getBasket(id,vendorId);
        handleResponse(exchange,response);
    }
    private void handleAddToBasket(HttpExchange exchange, Long id, Long foodId) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject input = new Gson().fromJson(reader, JsonObject.class);
        int quantity;
        try {
             quantity = input.get("quantity").getAsInt();
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid quantity\"\n}");
            return;
        }
        if(quantity <= 0) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid quantity\"\n}");
            return;
        }
        BasketItem basketItem = new BasketItem();
        basketItem.setQuantity(quantity);
        JsonObject response = basketDAO.addToBasket(id,foodId,basketItem);
        handleResponse(exchange,response);
    }
    private void handleRemoveFromBasket(HttpExchange exchange, Long id, Long basketItemId) throws IOException {
        JsonObject response = basketDAO.removeFromBasket(id,basketItemId);
        handleResponse(exchange,response);
    }
}
