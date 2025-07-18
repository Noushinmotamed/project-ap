package com.httpHandler;

import com.DAO.FoodItemDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.models.FoodItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;

import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;

public class FoodHttpHandler implements HttpHandler {

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (JwtUtil.validateToken(token) == null) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized request\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (method.equals("GET") && path.matches("^/foods/\\d+$")) {
            try {
                String[] pathParts = path.split("/");
                Long foodId = Long.parseLong(pathParts[2]);

                FoodItem item = foodItemDAO.findById(foodId);

                if (item != null) {
                    handleResponse(exchange, gson.toJsonTree(item).getAsJsonObject());
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Food item not found\"}");
                }

                return;

            } catch (NumberFormatException e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\":\"Invalid Food ID format\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"An unexpected server error occurred.\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        }
    }
}