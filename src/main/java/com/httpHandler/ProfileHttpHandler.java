package com.httpHandler;

import com.DAO.UserDAO;
import com.controller.UserController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.models.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.HibernateUtil;
import com.util.JwtUtil;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;                                                      //BUGGED!!  GET STILL FETCHES PROFILE WITH OLD PHONE NUMBER TOO . Handle email in Update!!

import static com.httpHandler.WalletHttpHandler.unauthorized;
import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;



public class ProfileHttpHandler implements HttpHandler {
    UserDAO userDAO = new UserDAO();
    UserController userController = new UserController();

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

        if (path.equals("/auth/profile")) {
            switch (method) {
                case "GET":
                    handleGetProfile(exchange, id);
                    break;
                case "PUT":
                    handleUpdateProfile(exchange, id);
                    break;

                default:
                    sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                    return;
            }

        } else {
            sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
            return;
        }

    }

    private void handleGetProfile(HttpExchange exchange, String id) throws IOException {
        JsonObject response = userDAO.getUser(Long.parseLong(id));
        handleResponse(exchange, response);

    }

    private void handleUpdateProfile(HttpExchange exchange, String id) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject input = new Gson().fromJson(reader, JsonObject.class);
        JsonObject response = userController.updateProfile(input, Long.parseLong(id));
        reader.close();
        handleResponse(exchange, response);
    }
}
