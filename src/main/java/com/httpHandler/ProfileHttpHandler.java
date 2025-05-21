package com.httpHandler;

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
import java.nio.charset.StandardCharsets;                                                      //BUGGED!! : GET STILL FETCHES PROFILE WITH OLD PHONE NUMBER TOO . Handle email in Update!!


public class ProfileHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            String response = "{\"error\":\"Unauthorized\"}";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
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
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, responseBytes.length);  // 401 Unauthorized
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
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
                    exchange.sendResponseHeaders(405, 0);   // method not allowed
            }

        } else {
            exchange.sendResponseHeaders(404, 0);      //not found
        }

    }

    private void handleGetProfile(HttpExchange exchange, String id) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Long userId = Long.valueOf(id);
            User user = session.get(User.class, userId);
            if (user == null) {
                exchange.sendResponseHeaders(404, -1); // Not Found
                return;
            }


            JsonObject response = new JsonObject();
            response.addProperty("id", user.getId().toString());
            response.addProperty("fullName", user.getFullName());
            response.addProperty("phone", user.getPhone());
            response.addProperty("email", user.getEmail());
            response.addProperty("role", user.getRole().toString().toLowerCase());
            response.addProperty("address", user.getAddress());
            response.addProperty("profileImageUrl", user.getImageBase64());


            if (user.getBankInfo() != null) {
                JsonObject bankInfo = new JsonObject();
                bankInfo.addProperty("bankName", user.getBankInfo().getBankName());
                bankInfo.addProperty("accountNumber", user.getBankInfo().getAccountNumber());
                response.add("bankInfo", bankInfo);
            }


            String responseBody = new Gson().toJson(response);


            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }


        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    private void handleUpdateProfile(HttpExchange exchange, String id) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject updatedProfile = new Gson().fromJson(reader, JsonObject.class);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Long userId = Long.valueOf(id);
            User user = session.get(User.class, userId);
            if (user == null) {
                exchange.sendResponseHeaders(404, -1);  // not found
                return;
            }

            if (updatedProfile.has("fullName")) {
                user.setFullName(updatedProfile.get("fullName").getAsString());
            }
//            if (updatedProfile.has("phone")) {
//                String number = updatedProfile.get("phone").getAsString();
//                if (!isValidNumber(number, session)) {
//
//                    String response = "{\"error\":\"Unauthorized\"}";
//                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
//                    exchange.getResponseHeaders().set("Content-Type", "application/json");
//                    exchange.sendResponseHeaders(401, responseBytes.length);  // 401 Unauthorized
//                    try (OutputStream os = exchange.getResponseBody()) {
//                        os.write(response.getBytes());
//                    }
//                    return;
//                } else {
//                    user.setPhone(number);
//                }
//            }
            if (updatedProfile.has("address")) {
                user.setAddress(updatedProfile.get("address").getAsString());
            }
            if(updatedProfile.has("bankInfo") && !updatedProfile.get("bankInfo").isJsonNull() && (user.getRole().toString().equalsIgnoreCase("seller") || user.getRole().toString().equalsIgnoreCase("courier"))) {
                JsonObject bankInfoJson = updatedProfile.getAsJsonObject("bankInfo");

                // Create or update BankInfo object
                BankInfo bankInfo = user.getBankInfo() != null ? user.getBankInfo() : new BankInfo();

                // Set fields with null checks
                if (bankInfoJson.has("bankName")) {
                    bankInfo.setBankName(bankInfoJson.get("bankName").getAsString());
                }
                if (bankInfoJson.has("accountNumber")) {
                    bankInfo.setAccountNumber(bankInfoJson.get("accountNumber").getAsString());
                }

                user.setBankInfo(bankInfo);
            }
            if (updatedProfile.has("profileImageBase64")) user.setImageBase64(updatedProfile.get("profileImageBase64").getAsString());
          if(user.getRole().toString().toLowerCase().equals("owner")) {

                        if(updatedProfile.has("brandName")) user.setBrandName(updatedProfile.get("brandName").getAsString());
                        if(updatedProfile.has("explanation")) user.setExplanation(updatedProfile.get("explanation").getAsString());


            }

            if(updatedProfile.has("email") && isValidEmail(updatedProfile.get("email").getAsString())){
                user.setEmail(updatedProfile.get("email").getAsString());
            }else if(updatedProfile.has("email") && !isValidEmail(updatedProfile.get("email").getAsString())){
                String response = "{\"error\":\"Invalid profile data\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
            session.saveOrUpdate(user);
            tx.commit();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message","Profile updated successfully");
            String response = new Gson().toJson(jsonObject);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }


        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }



    private boolean isValidNumber(String number, Session session) {
        if(isValidNumber(number)) {
            try {
                Long count = session.createQuery(
                                "SELECT COUNT(u) FROM User u WHERE u.phone = :phoneNumber",
                                Long.class)
                        .setParameter("phoneNumber", number)
                        .getSingleResult();

                return count == 0; // True if number doesn't exist
            } catch (NoResultException e) {
                return true; // No users found with this number
            }
        }else return false;
    }

    static boolean isValidNumber(String number) {
        if (number == null) {
            return false;
        } else if (number.length() == 10) {
            try {
                Double.parseDouble(number);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return false;

        }
    }

    public boolean isValidEmail(String email) {
        return UserHttpHandler.isValidEmail(email);
    }

}

