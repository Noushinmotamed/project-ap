package com.httpHandler;

import com.DAO.RatingDAO;
import com.controller.RatingController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;

public class RatingHttpHandler implements HttpHandler {

    private final RatingController controller = new RatingController();
    private final RatingDAO dao = new RatingDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
            return;
        }
       String path = exchange.getRequestURI().getPath();
       String method = exchange.getRequestMethod();
       String[] parts = path.split("/");
       if(path.equals("/ratings")) {
           if (method.equals("POST")) {
                 handleAddComment(exchange,Long.parseLong(id));
           }else{
               sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
               return;
           }
       }else if(path.startsWith("/ratings/items") && parts.length == 4) {
           String orderId = parts[3];
           try{
               handleGetRatings(exchange,Long.parseLong(orderId),Long.parseLong(id));
           } catch (Exception e) {
               sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
               return;
           }
       }else if(path.startsWith("/ratings/") && parts.length == 3) {
           Long ratingId;
           try{
               ratingId = Long.parseLong(parts[2]);
           }catch (Exception e) {
               sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
               return;
           }
           switch (method) {
               case "GET":
                   handleGetRating(exchange,Long.parseLong(id),ratingId);
                   break;
               case "PUT":
                   handleEditRating(exchange,Long.parseLong(id),ratingId);
                   break;
                   case "DELETE":
                       handleDeleteRating(exchange,Long.parseLong(id),ratingId);
                       break;
                       default:
                           sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");

           }
       }else if(path.startsWith("/ratings/orders") && parts.length == 4) {
           if(method.equals("GET")) {
               String orderId = parts[3];
               getRatingByOrder(exchange,Long.parseLong(id),Long.parseLong(orderId));
           }else{
               sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
           }
       }else{
           sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
       }
    }
    private void handleAddComment(HttpExchange exchange, Long userId) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject input = new Gson().fromJson(reader, JsonObject.class);
        if(!input.has("order_id")){
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid order_id\"\n}");
            return;
        }
        if(!input.has("comment")){
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid comment\"\n}");
            return;
        }
        if(!input.has("rating")){
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid rating\"\n}");
            return;
        }
        JsonObject response = controller.addRating(input,userId);
        reader.close();
        handleResponse(exchange, response);

    }
    private void handleGetRatings(HttpExchange exchange, Long orderId, Long userId) throws IOException {
        JsonObject response = dao.getRatings(orderId,userId);
        handleResponse(exchange, response);
    }
    private void handleGetRating(HttpExchange exchange, Long userId, Long ratingId) throws IOException {
        JsonObject response = dao.getRating(userId,ratingId);
        handleResponse(exchange, response);
    }
    private void handleEditRating(HttpExchange exchange, Long userId, Long ratingId) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject input = new Gson().fromJson(reader, JsonObject.class);
        JsonObject response = controller.editRating(input, userId,ratingId);
        handleResponse(exchange, response);
    }
    private void handleDeleteRating(HttpExchange exchange, Long userId, Long ratingId) throws IOException {
        JsonObject response = dao.deleteRating(userId,ratingId);
        handleResponse(exchange, response);
    }
    private void getRatingByOrder(HttpExchange exchange, Long id, Long orderId) throws IOException {
        JsonObject response = dao.getRatingByOrder(id,orderId);
        handleResponse(exchange, response);

    }
}
