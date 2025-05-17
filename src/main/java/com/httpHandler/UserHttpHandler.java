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

public class UserHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "POST":
                String path = exchange.getRequestURI().getPath();

                if (path.equals("/user/signup")) {
                    handleSignup(exchange);
                } else if (path.equals("/user/login")) {
                    handleLogin(exchange);
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }

                break;
            case "DELETE":
                //handle log out here?
            default:
                exchange.sendResponseHeaders(405, -1);  // Method Not Allowed

        }

    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        User user = new Gson().fromJson(reader, User.class);
        if(!isValidEmail(user.getEmail()) || !isValidPhoneNumber(user.getPhoneNumber())) {
            String response = "invalid email or phone number";
            exchange.sendResponseHeaders(406, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;

        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            if (isPhoneNumberTaken(session, user.getPhoneNumber())) {
                String response = "Username is already taken!";
                exchange.sendResponseHeaders(400, response.getBytes().length); // 400 Bad Request
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }                                                                      //save user directly to db,also create basket,profile and wallet

            session.save(user);
            switch(user.getRole()){
                case CUSTOMER:
                    Profile profile = new CustomerProfile(user.getAddress(),user.getId(), user.getFullName(), user.getPhoneNumber());
                    Basket basket = new Basket(user.getId());
                    Wallet wallet = new Wallet(user.getId());
                    session.save(profile);
                    session.save(basket);
                    session.save(wallet);
                    break;
                case OWNER:
                    Profile ownerProfile = new OwnerProfile(user.getAddress(),user.getId(), user.getFullName(), user.getPhoneNumber());
                    session.save(ownerProfile);
                    break;
                    case ADMIN:
                        Profile adminProfile = new AdminProfile(user.getId(), user.getFullName(), user.getPhoneNumber());
                        session.save(adminProfile);
                        break;
                        case COURTIER:
                            Profile courtierProfile = new CourtierProfile(user.getId(), user.getFullName(), user.getPhoneNumber());
                            session.save(courtierProfile);
                            System.out.println("Saving CourtierProfile for user: " + user.getFullName());
                            break;
            }
            transaction.commit();
            String response = "User has been signed up successfully!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        }catch (Exception e) {
            e.printStackTrace();
            String response = "Failed to create user";
            exchange.sendResponseHeaders(500, response.getBytes().length); // 500 Internal Server Error
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    private void handleLogin(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        User userToLogIn = new Gson().fromJson(reader, User.class);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if(!isValidPhoneNumber(userToLogIn.getPhoneNumber())) {
                String response = "Invalid phone number";
                exchange.sendResponseHeaders(401, response.getBytes().length); // 401 Unauthorized
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
            User foundUser = getUserByPhoneNumber(session,userToLogIn.getPhoneNumber());
            if(foundUser== null || !userToLogIn.getPassword().equals(foundUser.getPassword()) ){
                String response = "Invalid phone number or password!";
                exchange.sendResponseHeaders(401, response.getBytes().length); // 401 Unauthorized
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
            String token = generateJwtToken(foundUser);
              JsonObject jsonObject = new JsonObject();
              jsonObject.addProperty("token", token);
              jsonObject.addProperty("message","user logged in successfully!");
              String response = new Gson().toJson(jsonObject);

            exchange.sendResponseHeaders(200, response.getBytes().length); // 200 OK
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        } catch (Exception e) {
            e.printStackTrace();
            String response = "Failed to login";
            exchange.sendResponseHeaders(500, response.getBytes().length); // 500 Internal Server Error
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    private boolean isPhoneNumberTaken(Session session, String phoneNumber){
        User user = session.createQuery("from User where phoneNumber = :phoneNumber", User.class).setParameter("phoneNumber", phoneNumber).uniqueResult();
        return user!=null ;


    }
    private User getUserByPhoneNumber(Session session, String phoneNumber){
       return session.createQuery("from User where phoneNumber = :phoneNumber", User.class).setParameter("phoneNumber", phoneNumber).uniqueResult();
    }
    public static boolean isValidEmail(String email){
        if (email == null || email.isEmpty()) {
            return true;
        }
        return email.contains("@") && email.contains(".");
    }

    /**
     *
     * input format for phone number : [+98][user input example : 9194772344]
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
