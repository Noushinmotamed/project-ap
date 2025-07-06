package com.httpHandler;


import com.DAO.AdminDAO;
import com.controller.AdminController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.models.CourierStatus;
import com.models.OrderStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;


                                                                           // IF ANY QUERY IS INVALID NOT FOUND OR INVALID ???
                                                                          // I AM LETTING ADMIN UPDATE STATUS REGARDLESS OF USER'S TYPE ( IF I HAVE TO HANDLE, THEN WHAT WOULD THE ERROR BE ?)

/**
 * endpoints : admin/users
 *            admin/users/{id}/status
 *            admin/transactions
 *            admin/orders
 */
public class AdminHttpHandler implements HttpHandler {
    private AdminController adminController = new AdminController();
    private AdminDAO adminDAO = new AdminDAO();

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

        Long adminId = Long.parseLong(id);

        if (path.equals("/admin/users")) {
            if(method.equals("GET")) {
                handleGetUsers(exchange,adminId);

            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                return;
            }

        } else if (path.startsWith("/admin/orders")) {
            if(method.equals("GET")) {
                handleGetOrders(exchange,adminId);

            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                return;
            }

        } else if (path.startsWith("/admin/transactions")) {
            if(method.equals("GET")) {
                handleGetTransactions(exchange,adminId);

            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                return;
            }


        } else {
            String[] pathParts = path.split("/");
            if(pathParts.length == 5 && pathParts[2].equals("users") && pathParts[4].equals("status")) {
            if(!method.equals("PATCH")) {
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                return;

                }
                Long userId = null;
                try{
                    userId = Long.parseLong(pathParts[3]);
                }catch(NumberFormatException e){
                    sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
                    return;
                }
                handleUpdateUserStatus(exchange,adminId,userId);

            }else{
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
                return;
            }

        }
    }

    /**
     *  get list of ALL users
     *
     * @param id adminId
     * @throws IOException not handled
     */
    private void handleGetUsers(HttpExchange exchange, Long id) throws IOException {
        JsonElement response = adminDAO.getUsers(id);
        handleResponse(exchange,response);

    }
    private void handleUpdateUserStatus(HttpExchange exchange, Long adminId, Long userId) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject body = null;
        CourierStatus status = null;
        try {
            body = new Gson().fromJson(reader, JsonObject.class);
        }catch (JsonSyntaxException e){
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid status\"\n}");
            return;
        }
        if(body.has("status")) {
           try {


                status = CourierStatus.valueOf(body.get("status").getAsString());
           }catch (IllegalArgumentException e){
               sendResponse(exchange, 400, "{\n  \"error\":\"Invalid status\"\n}");
               return;
           }
        }else{
            sendResponse(exchange,400, "{\n  \"error\":\"Invalid status\"\n}");
            return;
        }
        reader.close();
        JsonObject response = adminDAO.updateStatus(adminId,userId,status);
        handleResponse(exchange,response);


    }

    /**
     *  get list of orders with optional filter
     *
     * @param id adminId
     * @throws IOException not handled
     */
    private void handleGetOrders(HttpExchange exchange, Long id) throws IOException {
        Map<String, String> queryParams = parseQueryParameters(exchange.getRequestURI().getQuery());
        String search = queryParams.get("search");
        String vendor = queryParams.get("vendor");
        String courier = queryParams.get("courier");
        String customer = queryParams.get("customer");
        String status = queryParams.get("status");
        JsonElement response = adminController.getOrders(id,search,vendor,courier,customer,status);
        handleResponse(exchange,response);




    }

    /**
     * get list of transactions with optional filter
     *
     * @param id adminId
     * @throws IOException not handled
     */
    private void handleGetTransactions(HttpExchange exchange, Long id) throws IOException {
        Map<String, String> queryParams = parseQueryParameters(exchange.getRequestURI().getQuery());
        String search = queryParams.get("search");
        String user = queryParams.get("user");
        String method = queryParams.get("method");
        String status = queryParams.get("status");
        JsonElement response = adminController.getTransactions(id,search,user,method,status);
        handleResponse(exchange,response);

    }

    /**
     * method for handling query in path
     * @param query the query in path
     * @return map of titles and values
     */
    public static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2); // Split on first "=" only
                String key = keyValue[0];
                String value = keyValue.length > 1 ? keyValue[1] : "";

                // Decode only spaces (%20 → " ", + → " ")
                key = decodeSpaces(key);
                value = decodeSpaces(value);

                queryParams.put(key, value);
            }
        }
        return queryParams;
    }

    private static String decodeSpaces(String str) {
        // Replace %20 with space
        str = str.replace("%20", " ");
        // Replace + with space (optional, since some systems use + for spaces)
        str = str.replace("+", " ");
        return str;
    }
}
