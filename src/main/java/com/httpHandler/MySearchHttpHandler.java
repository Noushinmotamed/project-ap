package com.httpHandler;

import com.DAO.MySearchDAO;
import com.controller.MySearchController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static com.httpHandler.AdminHttpHandler.parseQueryParameters;
import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;
                                                         //I ASSUME SEARCH IS NAME ???????
public class MySearchHttpHandler implements HttpHandler {
    private final MySearchController controller = new MySearchController();
    private final MySearchDAO dao = new MySearchDAO();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        String method = exchange.getRequestMethod();
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
            return;
        }
        if(path.equals("/items")) {
            if(method.equals("POST")){
                handleGetItems(exchange,Long.parseLong(id));
            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
            }
        }else if(path.startsWith("/items")) {
            if(method.equals("GET")){
            Long itemId;
            try {
                itemId = Long.parseLong(pathParts[2]);
                handleGetItem(exchange, Long.parseLong(id), itemId);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
            }
        }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
            }
        }else if(path.equals("/vendors")) {
            if(method.equals("POST")){
                handleGetVendors(exchange,Long.parseLong(id));
            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
            }

        }else if(path.startsWith("/vendors") && pathParts.length == 3) {
            try{
                Long vendorId = Long.parseLong(pathParts[2]);
                if(method.equals("GET")) {
                    handleGetVendor(exchange, Long.parseLong(id), vendorId);
                }else{
                    sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
            }

        }else{
            sendResponse(exchange, 405, "{\n  \"error\":\"Resource not found\"\n}");
        }
    }
    private void handleGetItems(HttpExchange exchange, long id) throws IOException {
        try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            JsonObject input = new Gson().fromJson(reader, JsonObject.class);
            JsonElement response = controller.getItems(input, id);
            handleResponse(exchange, response);
        }

    }
    private void handleGetItem(HttpExchange exchange, long id, Long itemId) throws IOException {
        JsonObject response = dao.getItem(id,itemId);
        handleResponse(exchange, response);
    }
    private void handleGetVendors(HttpExchange exchange, Long id) throws IOException {
        try(InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            JsonObject input = new Gson().fromJson(reader, JsonObject.class);
            JsonElement response = controller.getVendors(input,id);
            handleResponse(exchange, response);

        }
    }
    private void handleGetVendor(HttpExchange exchange, Long id, Long vendorId) throws IOException {
       JsonObject response = dao.getVendor(id,vendorId);
       handleResponse(exchange, response);
    }
}
