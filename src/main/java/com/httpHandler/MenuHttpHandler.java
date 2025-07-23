package com.httpHandler;

import com.DAO.MenuDAO;
import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;

import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;

public class MenuHttpHandler implements HttpHandler {

    private final MenuDAO menuDAO = new MenuDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (JwtUtil.validateToken(token) == null) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized request\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (method.equals("GET") && path.matches("^/menus/\\d+/items$")) {
            try {
                String[] pathParts = path.split("/");
                Long menuId = Long.parseLong(pathParts[2]);

                JsonArray items = menuDAO.getFoodItemsByMenuId(menuId);
                handleResponse(exchange, items);

            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid Menu ID format\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        }
    }
}