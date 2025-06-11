package com.httpHandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.models.*;
import com.util.HibernateUtil;
import com.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReviewHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "POST":
                    postHandler(exchange);
                    break;
                case "GET":
                    if (path.matches("/ratings/\\d+")) {
                        getByIdHandler(exchange);
                    }
                    if (path.matches("/ratings/items/\\d+")) {
                        getByItemIdHandler(exchange);
                    }
                    break;
                case "PUT":
                    if (path.matches("/ratings/\\d+")) {
                        updateHandler(exchange);
                        return;
                    }
                    break;
                case "DELETE":
                    if (path.matches("/ratings/\\d+")) {
                        deleteHandler(exchange);
                        return;
                    }
                    break;
                default:
                    sendError(exchange, 405); // Method Not Allowed
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500);
        }
    }

    private void postHandler(HttpExchange exchange) {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || token.isEmpty()) {
                sendError(exchange, 401);
                return;
            }

            String userIdStr = JwtUtil.validateToken(token);
            if (userIdStr == null) {
                sendError(exchange, 401);
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            StringBuilder bodyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }

            Gson gson = new Gson();
            ReviewRequest body = gson.fromJson(bodyBuilder.toString(), ReviewRequest.class);

            if (body.order_id == null || body.rating == null || body.comment == null) {
                sendError(exchange, 400);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();

                Order order = session.get(Order.class, body.order_id);
                if (order == null || !order.getCustomerId().equals(userId)) {
                    sendError(exchange, 403);
                    return;
                }

                List<OrderItem> items = order.getItems();
                if (items.isEmpty()) {
                    sendError(exchange, 400);
                    return;
                }

                OrderItem food = items.getFirst();

                Query<Long> query = session.createQuery(
                        "SELECT COUNT(*) FROM Review,Food,User WHERE Food.foodId = :foodId AND User.id = :userId",
                        Long.class);
                query.setParameter("foodId", food.getFoodId());
                query.setParameter("userId", userId);

                if (query.uniqueResult() > 0) {
                    sendError(exchange, 409); // Conflict
                    return;
                }

                Review review = new Review();
                review.setRating(body.rating);
                review.setText(body.comment);
                review.setImageUrl((body.imageBase64 != null && !body.imageBase64.isEmpty()) ? body.imageBase64.get(0) : null);
                review.setUser(userId);
                review.setFood(food.getFoodId());

                session.persist(review);
                session.getTransaction().commit();

                String response = gson.toJson(review);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500);
        }
    }

    private void getByIdHandler(HttpExchange exchange) {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || token.isEmpty()) {
                sendError(exchange, 401);
                return;
            }

            String userIdStr = JwtUtil.validateToken(token);
            if (userIdStr == null) {
                sendError(exchange, 401);
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            String path = exchange.getRequestURI().getPath(); // /ratings/42
            String[] parts = path.split("/");
            if (parts.length < 3) {
                sendError(exchange, 400);
                return;
            }

            Long reviewId;
            try {
                reviewId = Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                sendError(exchange, 400);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Review review = session.get(Review.class, reviewId);
                if (review == null) {
                    sendError(exchange, 404);
                    return;
                }

                if (review.getUserid()!= (userId)) {
                    sendError(exchange, 403);
                    return;
                }

                Gson gson = new Gson();
                String json = gson.toJson(review);
                byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500);
        }
    }

    private void getByItemIdHandler(HttpExchange exchange) {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || token.isEmpty()) {
                sendError(exchange, 401);
                return;
            }

            String userIdStr = JwtUtil.validateToken(token);
            if (userIdStr == null) {
                sendError(exchange, 401);
                return;
            }

            String path = exchange.getRequestURI().getPath(); // /ratings/items/10
            String[] parts = path.split("/");
            if (parts.length < 4) {
                sendError(exchange, 400);
                return;
            }

            Long itemId;
            try {
                itemId = Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                sendError(exchange, 400);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Query<Review> query = session.createQuery(
                        "FROM Review r WHERE r.foodid= :itemId", Review.class);
                query.setParameter("itemId", itemId);
                List<Review> reviews = query.list();

                if (reviews.isEmpty()) {
                    sendError(exchange, 404);
                    return;
                }

                double avgRating = reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("avg_rating", avgRating);
                responseMap.put("comments", reviews);

                Gson gson = new Gson();
                String json = gson.toJson(responseMap);
                byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500);
        }
    }

    private void deleteHandler(HttpExchange exchange) {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || JwtUtil.validateToken(token) == null) {
                sendError(exchange, 401);
                return;
            }

            String[] parts = exchange.getRequestURI().getPath().split("/");
            Long id = Long.parseLong(parts[2]);

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Review review = session.get(Review.class, id);
                if (review == null) {
                    sendError(exchange, 404);
                    return;
                }

                session.beginTransaction();
                session.remove(review);
                session.getTransaction().commit();

                Gson gson = new Gson();
                String json = gson.toJson(review);
                byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500);
        }
    }

    private void updateHandler(HttpExchange exchange) {
        try {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            if (token == null || JwtUtil.validateToken(token) == null) {
                sendError(exchange, 401);
                return;
            }

            String[] parts = exchange.getRequestURI().getPath().split("/");
            Long id = Long.parseLong(parts[2]);

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(body, JsonObject.class);

            int rating = json.has("rating") ? json.get("rating").getAsInt() : -1;
            String comment = json.has("comment") ? json.get("comment").getAsString() : null;

            if (rating < 1 || rating > 5) {
                sendError(exchange, 400);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Review review = session.get(Review.class, id);
                if (review == null) {
                    sendError(exchange, 404);
                    return;
                }

                session.beginTransaction();
                review.setRating(rating);
                if (comment != null) review.setText(comment);
                session.merge(review);
                session.getTransaction().commit();

                Map<String, Object> result = new HashMap<>();
                result.put("id", review.getReviewId());
                result.put("rating", review.getRating());
                result.put("comment", review.getText());
                result.put("user_id", review.getUserid());
                result.put("item_id", review.getFoodId());
                result.put("created_at", Instant.now().toString());

                Gson gson1 = new Gson();
                String json1 = gson1.toJson(review);
                byte[] responseBytes = json1.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500);
        }
    }


    private void sendError(HttpExchange exchange, int statusCode) {
        try {
            exchange.sendResponseHeaders(statusCode, -1);
        } catch (Exception ignored) {}
    }

    private static class ReviewRequest {
        Long order_id;
        Integer rating;
        String comment;
        List<String> imageBase64;
    }
}