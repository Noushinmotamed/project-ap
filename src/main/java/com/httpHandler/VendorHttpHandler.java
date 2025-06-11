package com.httpHandler;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.controller.RestaurantDTO;
import com.models.Restaurant;
import com.models.User;
import com.models.Food;
import org.hibernate.Session;
import org.hibernate.query.Query;
import com.util.HibernateUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class VendorHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/vendors".equals(path) && "POST".equalsIgnoreCase(method)) {
            handleSearchVendors(exchange);
        } else if (path.matches("^/vendors/\\d+$") && "GET".equalsIgnoreCase(method)) {
            handleGetVendorMenu(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    private void handleSearchVendors(HttpExchange exchange) {
        try (
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                JsonReader reader = new JsonReader(isr);
                Session session = HibernateUtil.getSessionFactory().openSession()
        ) {
            JsonObject jsonRequest = JsonParser.parseReader(reader).getAsJsonObject();

            String search = jsonRequest.has("search") ? jsonRequest.get("search").getAsString().toLowerCase() : null;
            JsonArray keywordsArray = jsonRequest.has("keywords") ? jsonRequest.getAsJsonArray("keywords") : null;

            // ساخت HQL داینامیک
            StringBuilder hql = new StringBuilder("SELECT DISTINCT f.restaurant FROM Food f WHERE 1=1");

            if (search != null && !search.isEmpty()) {
                hql.append(" AND (LOWER(f.restaurant.name) LIKE :search OR LOWER(f.name) LIKE :search)");
            }

            Map<String, Object> keywordParams = new HashMap<>();
            if (keywordsArray != null && keywordsArray.size() > 0) {
                hql.append(" AND (");
                for (int i = 0; i < keywordsArray.size(); i++) {
                    String paramName = "kw" + i;
                    hql.append("LOWER(f.name) LIKE :").append(paramName);
                    if (i < keywordsArray.size() - 1) {
                        hql.append(" OR ");
                    }
                    keywordParams.put(paramName, "%" + keywordsArray.get(i).getAsString().toLowerCase() + "%");
                }
                hql.append(")");
            }

            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);

            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            for (Map.Entry<String, Object> entry : keywordParams.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            List<Restaurant> result = query.getResultList();

            String jsonResponse = new Gson().toJson(result);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1); // Internal Server Error
            } catch (IOException ignore) {}
        }
    }


    private void handleGetVendorMenu(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Long restaurantId;
        try {
            restaurantId = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            sendStatus(exchange, 400); // Bad Request
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                sendStatus(exchange, 404); // Not Found
                return;
            }

            Query<Food> query = session.createQuery(
                    "FROM Food f WHERE f.restaurantId = :rid", Food.class
            );
            query.setParameter("rid", restaurantId);
            List<Food> foods = query.getResultList();

            Map<String, List<Food>> grouped = foods.stream()
                    .collect(Collectors.groupingBy(Food::getCategory));

            JsonObject response = new JsonObject();
            response.add("vendor", new Gson().toJsonTree(restaurant));

            JsonArray menuTitles = new JsonArray();
            for (String title : grouped.keySet()) {
                menuTitles.add(title);
                response.add(title, new Gson().toJsonTree(grouped.get(title)));
            }
            response.add("FoodCategory", menuTitles);

            String json = new Gson().toJson(response);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendStatus(exchange, 500); // Internal Server Error
        }
    }

    private void sendStatus(HttpExchange exchange, int status) {
        try {
            exchange.sendResponseHeaders(status, -1);
        } catch (IOException ignored) {}
    }


}
