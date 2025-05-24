package com.httpHandler;
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

import static com.httpHandler.ProfileHttpHandler.isValidNumber;
import static com.util.JwtUtil.addToInvalid;

public class UserHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "POST":
                String path = exchange.getRequestURI().getPath();

                if (path.equals("/auth/register")) {
                    handleSignup(exchange);
                } else if (path.equals("/auth/login")) {
                    handleLogin(exchange);
                } else if(path.equals("/auth/logout")) {
                      handleLogout(exchange);
                }else{
                    String response = "not_found";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(404, responseBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                    }
                }

                break;
            default:
                String response = "method_not_allowed";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(405, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

        }

    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        User user = new Gson().fromJson(reader, User.class);
        if(user.getRole() ==null || user.getPassword() == null || user.getAddress() == null || user.getFullName() == null || user.getRole()== null){
            String response = "Invalid_input";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            return;
        }try{
            Role.valueOf(user.getRole().toString());
        }catch(IllegalArgumentException e){
            String response = "Invalid_input";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            return;
        }
        if(!isValidEmail(user.getEmail()) || !isValidPhoneNumber(user.getPhone())) {
            String response = "Invalid_input";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            return;

        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            if (isPhoneNumberTaken(session, user.getPhone())) {
                String response = "{\"error\":\"Phone number already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }                                                                      //save user directly to db,also create basket,profile and wallet


         if(user.getRole() == Role.seller || user.getRole() == Role.courier) {
                 if(user.getBankInfo()!= null && (user.getBankInfo().getBankName()== null || user.getBankInfo().getBankName().isEmpty() || user.getBankInfo().getAccountNumber()== null || user.getBankInfo().getAccountNumber().isEmpty())){
                     String response = "Invalid_input";
                     byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                     exchange.sendResponseHeaders(400, responseBytes.length);
                     try (OutputStream os = exchange.getResponseBody()) {
                         os.write(responseBytes);
                     }
                     return;
                }
            }else{
             user.setBankInfo(null);
         }
            session.save(user);
            if(user.getRole() == Role.buyer) {
                Basket basket = new Basket(user.getId());
                Wallet wallet = new Wallet(user.getId());
                session.save(basket);
                session.save(wallet);
            }

            transaction.commit();
            String token = generateJwtToken(user);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message","User registered successfully");
            jsonObject.addProperty("userId", user.getId());
            jsonObject.addProperty("token", token);
            String response = new Gson().toJson(jsonObject);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        }catch (Exception e) {
            e.printStackTrace();
            String response = "internal_server_error";
            exchange.sendResponseHeaders(500, response.getBytes().length); // 500 Internal Server Error
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    private void handleLogin(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject userToLogIn = new Gson().fromJson(reader, JsonObject.class);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if(!userToLogIn.has("phone") || !isValidPhoneNumber(userToLogIn.get("phone").getAsString()) || !userToLogIn.has("password")){
                String response = "Invalid_input";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            User foundUser = getUserByPhoneNumber(session,userToLogIn.get("phone").getAsString());
            if(foundUser== null || !userToLogIn.get("password").getAsString().equals(foundUser.getPassword()) ){
                String response = "{\"error\":\"invalid_input\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String token = generateJwtToken(foundUser);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "User logged in successfully");
            responseJson.addProperty("token", token);

            JsonObject userJson = new JsonObject();
            userJson.addProperty("id", foundUser.getId().toString());
            userJson.addProperty("fullName", foundUser.getFullName());
            userJson.addProperty("role", foundUser.getRole().toString());
            responseJson.add("user", userJson);

            String responseBody = new Gson().toJson(responseJson);
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }


        }catch (Exception e) {
            e.printStackTrace();
            String response = "internal_server_error";
            exchange.sendResponseHeaders(500, response.getBytes().length); // 500 Internal Server Error
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            String response = "{\"error\":\"Unauthorized\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(401, responseBytes.length);  // 401 Unauthorized
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            String response = "{\"error\":\"Unauthorized\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(401, responseBytes.length);  // 401 Unauthorized
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Long userId = Long.valueOf(id);
            User user = session.get(User.class, userId);
            if (user == null) {
                exchange.sendResponseHeaders(404, -1); // Not Found
                return;
            }
            JwtUtil.addToInvalid(token);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message","User logged out successfully");
            String response = new Gson().toJson(jsonObject);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }



        }catch(Exception e){
            exchange.sendResponseHeaders(500, -1);
            e.printStackTrace();
        }

    }
    private boolean isPhoneNumberTaken(Session session, String phoneNumber){
        User user = session.createQuery("from User where phone = :phoneNumber", User.class).setParameter("phoneNumber", phoneNumber).uniqueResult();
        return user!=null ;


    }
    private User getUserByPhoneNumber(Session session, String phoneNumber){
       return session.createQuery("from User where phone = :phoneNumber", User.class).setParameter("phoneNumber", phoneNumber).uniqueResult();
    }
    public static boolean isValidEmail(String email){
        if (email == null || email.isEmpty()) {
            return true;
        }
        return email.contains("@") && email.contains(".");
    }

    /**
     *
     * input format for phone number : [user input example : 09194772344]
     */
    public boolean isValidPhoneNumber(String phoneNumber){
    return isValidNumber(phoneNumber);
    }
    private String generateJwtToken(User user) {
        return JwtUtil.generateToken(user.getId().toString());
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
