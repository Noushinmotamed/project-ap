package com.httpHandler;
import com.controller.UserController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.models.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.HibernateUtil;
import com.util.JwtUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;                                                              //should i return the profile object?


import static com.controller.UserController.isValidNumber;
import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;
import static com.util.JwtUtil.addToInvalid;
import static com.util.JwtUtil.getKey;


public class UserHttpHandler implements HttpHandler {
    UserController userController = new UserController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/auth/register")) {
                handleSignup(exchange);
            } else if (path.equals("/auth/login")) {
                handleLogin(exchange);
            } else if (path.equals("/auth/logout")) {
                handleLogout(exchange);
            } else {
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
            }

        } else {
            sendResponse(exchange, 404, "{\n  \"error\":\"Method not allowed\"\n}");
            return;
        }

    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        User user = new User();
        JsonObject input = new Gson().fromJson(reader, JsonObject.class);
        if(input.has("phone")){
            user.setPhone(input.get("phone").getAsString());
        }else{
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid phone\"\n}");
            return;
        }
        if (input.has("password")) {
            user.setPassword(input.get("password").getAsString());
        } else {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid password\"\n}");
            return;
        }
        if (input.has("full_name")) {
            user.setFullName(input.get("full_name").getAsString());
        } else {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid full_name\"\n}");
            return;
        }
        if (input.has("address")) {
            user.setAddress(input.get("address").getAsString());
        } else {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid address\"\n}");
            return;
        }
        if (input.has("role")) {
            Role role;
            try {
                 role = Role.valueOf(input.get("role").getAsString());
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid role\"\n}");
                return;
            }
            user.setRole(role);
        } else {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid role\"\n}");
            return;
        }
        if (input.has("email")) {
            user.setEmail(input.get("email").getAsString());
        }
        if (input.has("profileImageBase64")) {
            user.setImageBase64(input.get("profileImageBase64").getAsString());
        }
        if (input.has("bank_info")) {
            JsonObject bankInfo = input.get("bank_info").getAsJsonObject();
            if (!bankInfo.has("account_number")) {
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid account_number\"\n}");
                return;
            } else if (!bankInfo.has("bank_name")) {
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid bank_name\"\n}");
                return;
            } else {

                user.setBankInfo(new BankInfo(bankInfo.get("bank_name").getAsString(), bankInfo.get("account_number").getAsString()));
            }
        }
        reader.close();
        JsonObject response = userController.addUser(user);
        handleResponse(exchange, response);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject userToLogIn = new Gson().fromJson(reader, JsonObject.class);
        if (!userToLogIn.has("password")) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid password\"\n}");
            return;
        }
        if (!userToLogIn.has("phone")) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid phone\"\n}");
            return;
        }
        String password = userToLogIn.get("password").getAsString();
        String phone = userToLogIn.get("phone").getAsString();
        JsonObject response = userController.logInUser(phone, password);
        reader.close();
        handleResponse(exchange, response);



    }

    private void handleLogout(HttpExchange exchange) throws IOException {

        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);

        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Long userId = Long.valueOf(id);
            User user = session.get(User.class, userId);
            if (user == null) {
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
                return;
            }


            sendResponse(exchange, 200, "{\n  \"message\":\"User logged out successfully\"\n}");


        } catch (Exception e) {
            sendResponse(exchange, 500, "{\n  \"error\":\"Internal server error\"\n}");
            e.printStackTrace();
            return;
        }

    }


}














//int HTTP_BAD_REQUEST = 400;
//int HTTP_UNAUTHORIZED = 401;
//int HTTP_PAYMENT_REQUIRED = 402;
//int HTTP_FORBIDDEN = 403;
//int HTTP_NOT_FOUND = 404;
//int HTTP_METHOD_NOT_ALLOWED = 405;
//int HTTP_NOT_ACCEPTABLE = 406;
//int HTTP_PROXY_AUTH_REQUIRED = 407;
//int HTTP_REQUEST_TIMEOUT = 408;
//GET POST PUT DELETE
