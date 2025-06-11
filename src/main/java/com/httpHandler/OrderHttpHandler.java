package com.httpHandler;

import com.google.gson.*;
import com.models.*;
import com.models.OrderStatus;
import com.util.HibernateUtil;
import com.util.JwtUtil;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.query.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class OrderHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || token.isEmpty()) {
                exchange.sendResponseHeaders(401, -1); // Unauthorized
                return;
            }

            String userId = JwtUtil.validateToken(token);
            if (userId == null) {
                exchange.sendResponseHeaders(401, -1); // Invalid Token
                return;
            }

            String path = exchange.getRequestURI().getPath(); // e.g. /orders/12
            String[] pathParts = path.split("/");

            if (path.equals("/orders")) {
                handleAllOrders(exchange, Long.parseLong(userId));
            } else if (path.equals("/orders/history")) {
                handleOrderHistory(exchange, Long.parseLong(userId));
            } else {
                Pattern pattern = Pattern.compile("/orders/(\\d+)");
                Matcher matcher = pattern.matcher(path);

                if (matcher.matches()) {
                    Long orderId = Long.parseLong(matcher.group(1));
                    handleOrderDetails(exchange, Long.parseLong(userId), orderId);
                } else {
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                }
            }

        } catch (SecurityException e) {
            try {
                exchange.sendResponseHeaders(403, -1); // Forbidden
            } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1); // Internal Server Error
            } catch (Exception ignored) {}
        }
    }

    private void handleAllOrders(HttpExchange exchange, Long userId) {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // خواندن JSON از Body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder bodyBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                bodyBuilder.append(line);
            }

            // تبدیل JSON به Map
            Gson gson = new Gson();
            Map<String, Object> body = gson.fromJson(bodyBuilder.toString(), Map.class);

            // بررسی ورودی‌های اجباری
            if (!body.containsKey("delivery_address") || !body.containsKey("vendor_id") || !body.containsKey("items")) {
                exchange.sendResponseHeaders(400, -1); // Bad Request
                return;
            }

            String deliveryAddress = body.get("delivery_address").toString();
            Long restaurantId = ((Number) body.get("vendor_id")).longValue();
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            if (items.isEmpty()) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }



            List<OrderItem> orderItems = new ArrayList<>();
            double totalPrice = 0;

            for (Map<String, Object> item : items) {
                Long foodId = ((Number) item.get("item_id")).longValue();
                int quantity = ((Number) item.get("quantity")).intValue();

                // فرض: قیمت غذا از دیتابیس خوانده می‌شود
                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                    Food food = session.get(Food.class, foodId);
                    if (food == null || !food.isavailable()) {
                        exchange.sendResponseHeaders(404, -1); // Not Found
                        return;
                    }

                    double finalPrice = food.getPrice() * quantity;

                    OrderItem orderItem = new OrderItem(foodId,food.getFoodName(),food.getPrice() * quantity,quantity);

                    orderItems.add(orderItem);
                    totalPrice += finalPrice;
                }
            }
            // ساخت سفارش و آیتم‌ها
            Order newOrder = new Order(userId,userId,deliveryAddress,(ArrayList<OrderItem>)orderItems,totalPrice,OrderStatus.RECEIVED,LocalDateTime.now());

            // ذخیره در دیتابیس
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(newOrder);
                session.getTransaction().commit();
            }

            // پاسخ موفق
            String response = gson.toJson(newOrder);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ignored) {}
        }
    }


    private Order handleOrderDetails(HttpExchange exchange, Long orderId, Long userId) throws SecurityException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE orderId = :id", Order.class);
            query.setParameter("id", orderId);

            Order order = query.getSingleResult();

            // بررسی اینکه سفارش متعلق به همین کاربر باشد
            if (!order.getCustomerId().equals(userId)) {
                throw new SecurityException("Forbidden access to this order");
            }

            return order;

        } catch (NoResultException e) {
            return null; // سفارش پیدا نشد
        }
    }

    private void handleOrderHistory(HttpExchange exchange, Long userId) {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // پارامترهای جستجو
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            Map<String, String> queryParams = parseQuery(query);

            String search = queryParams.getOrDefault("search", "").toLowerCase();
            String vendor = queryParams.getOrDefault("vendor", "").toLowerCase();

            // گرفتن سفارش‌های کاربر
            List<Order> result;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "FROM Order o WHERE o.customerId = :userId";
                if (!vendor.isEmpty()) {
                    hql += " AND lower(o.restaurant.name) LIKE :vendor";
                }

                Query<Order> queryObj = session.createQuery(hql, Order.class);
                queryObj.setParameter("userId", userId);
                if (!vendor.isEmpty()) {
                    queryObj.setParameter("vendor", "%" + vendor + "%");
                }

                result = queryObj.list();

                // فیلتر search روی آیتم‌های سفارش (در حافظه)
                if (!search.isEmpty()) {
                    result = result.stream()
                            .filter(order -> order.getItems().stream()
                                    .anyMatch(item -> item.getFoodName().toLowerCase().contains(search)))
                            .collect(Collectors.toList());
                }
            }

            // ارسال پاسخ
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(result);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ignored) {}
        }
    }
    private Map<String, String> parseQuery(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query == null || query.isEmpty()) return queryParams;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                queryParams.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return queryParams;
    }

}
