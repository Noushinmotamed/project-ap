package com.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class HttpUtil {
    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
            os.flush();

        }

    }

    public static void sendSimpleResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static void handleResponse(HttpExchange exchange, JsonObject response) throws IOException {
        String responseString = new GsonBuilder().setPrettyPrinting().create().toJson(response);

        if (response.has("error")) {
            String error = response.get("error").getAsString();
            if (error.contains("not found")) {
                sendResponse(exchange, 404, responseString);
            } else if (error.contains("Invalid")) {
                sendResponse(exchange, 400, responseString);
            } else if (error.contains("Internal server error")) {
                sendResponse(exchange, 500, responseString);
            } else if (error.contains("Forbidden")) {
                sendResponse(exchange, 403, responseString);
            } else if (error.contains("Conflict occurred")) {
                sendResponse(exchange, 409, responseString);
            }

        } else {
            sendResponse(exchange, 200, responseString);
        }

    }
    public static void handleResponse(HttpExchange exchange, JsonElement response) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String responseString = gson.toJson(response);

        if (response.isJsonObject() && response.getAsJsonObject().has("error")) {
            String error = response.getAsJsonObject().get("error").getAsString();

            if (error.contains("not found")) {
                sendResponse(exchange, 404, responseString);
            } else if (error.contains("Invalid")) {
                sendResponse(exchange, 400, responseString);
            } else if (error.contains("Internal server error")) {
                sendResponse(exchange, 500, responseString);
            } else if (error.contains("Forbidden")) {
                sendResponse(exchange, 403, responseString);
            } else if (error.contains("Conflict occurred")) {
                sendResponse(exchange, 409, responseString);
            }
        } else {
            // Handles both JsonArray and JsonObject responses
            sendResponse(exchange, 200, responseString);
        }
    }




}