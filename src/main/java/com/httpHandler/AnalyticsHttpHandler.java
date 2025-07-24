package com.httpHandler;

import com.DAO.RestaurantDAO;
import com.google.gson.JsonObject;
import com.models.User;
import com.models.Role;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.HibernateUtil;
import com.util.JwtUtil;
import org.hibernate.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class AnalyticsHttpHandler implements HttpHandler {

    private RestaurantDAO restaurantDAO = new RestaurantDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            handleGetRequest(exchange);
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized: Missing or malformed token\"}");
                return;
            }
            String token = authHeader.substring(7);

            String username = JwtUtil.validateToken(token);
            System.out.println(">>> [DEBUG] Token Subject (username) is: " + username);

            if (username == null) {
                sendResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid or expired token\"}");
                return;
            }

            User seller;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Long sellerIdFromToken = Long.parseLong(username);

                seller = session.createQuery("FROM User WHERE id = :userId", User.class)
                        .setParameter("userId", sellerIdFromToken)
                        .uniqueResult();
            }

            if (seller == null) {
                sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                return;
            }

            if (seller.getRole() != Role.seller) {
                sendResponse(exchange, 403, "{\"error\":\"Forbidden: Access denied for this role\"}");
                return;
            }

            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            String startDate = params.get("startDate");
            String endDate = params.get("endDate");

            if (startDate == null || endDate == null || startDate.isEmpty() || endDate.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\":\"startDate and endDate parameters are required\"}");
                return;
            }

            JsonObject analyticsData = restaurantDAO.getAnalytics(seller.getId(), startDate, endDate);

            if (analyticsData.has("error")) {
                sendResponse(exchange, 404, analyticsData.toString());
            } else {
                sendResponse(exchange, 200, analyticsData.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private Map<String, String> queryToMap(String query) {
        if (query == null) {
            return new HashMap<>();
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            try {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(entry[1], StandardCharsets.UTF_8.name());
                    result.put(key, value);
                }
            } catch (Exception e) {
                System.err.println("Could not parse query param: " + param);
            }
        }
        return result;
    }
}