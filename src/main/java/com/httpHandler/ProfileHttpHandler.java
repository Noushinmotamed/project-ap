package com.httpHandler;

import com.controller.CourtierProfileDTO;
import com.controller.OwnerProfileDTO;
import com.controller.ProfileDTO;
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
            exchange.sendResponseHeaders(401, -1); // Unauthorized
            return;
        }

        String id = JwtUtil.validateToken(token);
        if (id == null) {
            exchange.sendResponseHeaders(401, -1); // Unauthorized
            return;
        }
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/profile")) {
            switch (method) {
                case "GET":
                    handleGetProfile(exchange, id);
                    break;
                case "PATCH":
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

            ProfileDTO profileDTO=createDTOProfile(user,session,userId);

            String response = new Gson().toJson(profileDTO);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
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
            Profile profile = getProfileByRole(session, user);
            if (updatedProfile.has("fullName")) {
                profile.setFullName(updatedProfile.get("fullName").getAsString());
                user.setFullName(updatedProfile.get("fullName").getAsString());
            }
            if (updatedProfile.has("phoneNumber")) {
                String number = updatedProfile.get("phoneNumber").getAsString();
                if (!number.equals(profile.getPhoneNumber()) && !isValidNumber(number, session)) {

                    exchange.sendResponseHeaders(401, -1);
                    return;
                } else {
                    profile.setPhoneNumber(number);
                    user.setPhoneNumber(number);
                }
            }
            if (updatedProfile.has("address")) {
                profile.setAddress(updatedProfile.get("address").getAsString());
                user.setAddress(updatedProfile.get("address").getAsString());
            }
            if (updatedProfile.has("imageUrl")) profile.setImageUrl(updatedProfile.get("imageUrl").getAsString());
            switch(user.getRole()){
                    case OWNER:
                        if(updatedProfile.has("bankInfo")) ((OwnerProfile)profile).setBankInfo(updatedProfile.get("bankInfo").getAsString());
                        if(updatedProfile.has("brandName")) ((OwnerProfile)profile).setBrandName(updatedProfile.get("brandName").getAsString());
                        if(updatedProfile.has("explanation")) ((OwnerProfile)profile).setExplanation(updatedProfile.get("explanation").getAsString());
                        break;
                            case COURTIER:
                                if(updatedProfile.has("bankInfo")) ((CourtierProfile)profile).setBankInfo(updatedProfile.get("bankInfo").getAsString());
                                if(updatedProfile.has("courtierStatus")) ((CourtierProfile)profile).setStatus(CourtierStatus.valueOf(updatedProfile.get("courtierStatus").getAsString().toUpperCase()));
                                break;
            }
            ProfileDTO profileDTO=createDTOProfile(user,session,userId);
            if(updatedProfile.has("email") && isValidEmail(updatedProfile.get("email").getAsString())){
                profileDTO.setEmail(updatedProfile.get("email").getAsString());
                user.setEmail(updatedProfile.get("email").getAsString());
            }else if(!isValidEmail(updatedProfile.get("email").getAsString())){
                exchange.sendResponseHeaders(401, -1);
                return;
            }
            session.saveOrUpdate(profile);
            tx.commit();


            String response = new Gson().toJson(profileDTO);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1); // Internal Server Error
        }
    }

    private Profile getProfileByRole(Session session, User user) {
        Profile profile = null;
        switch (user.getRole()) {

            case ADMIN:
                profile = session.createQuery("from AdminProfile where userId = :userId", AdminProfile.class).setParameter("userId", user.getId()).getSingleResult();
                break;
            case OWNER:
                profile = session.createQuery("from OwnerProfile where userId = :userId", OwnerProfile.class).setParameter("userId", user.getId()).getSingleResult();
                break;
            case CUSTOMER:
                profile = session.createQuery("from CustomerProfile where userId = :userId", CustomerProfile.class).setParameter("userId", user.getId()).getSingleResult();
                break;
            case COURTIER:
                profile = session.createQuery("from CourtierProfile where userId = :userId", CourtierProfile.class).setParameter("userId", user.getId()).getSingleResult();
                break;

        }
        return profile;
    }

    private boolean isValidNumber(String number, Session session) {
        if(isValidNumber(number)) {
            try {
                Long count = session.createQuery(
                                "SELECT COUNT(u) FROM User u WHERE u.phoneNumber = :phoneNumber",
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
    private ProfileDTO createDTOProfile(User user,Session session,Long userId) {
        Profile profile = null;
        ProfileDTO profileDTO = null;
        switch (user.getRole()) {
            case ADMIN:
                profile = session.createQuery("from AdminProfile where userId = :userId", AdminProfile.class).setParameter("userId", userId).getSingleResult();
                profileDTO = new ProfileDTO(
                        profile.getFullName(),
                        profile.getPhoneNumber(),
                        profile.getAddress(),
                        profile.getImageUrl(),
                        user.getEmail()
                );
                break;
            case OWNER:
                profile = session.createQuery("from OwnerProfile where userId = :userId", OwnerProfile.class).setParameter("userId", userId).getSingleResult();
                profileDTO = new OwnerProfileDTO(
                        profile.getFullName(),
                        profile.getPhoneNumber(),
                        profile.getAddress(),
                        profile.getImageUrl(),
                        user.getEmail(),
                        ((OwnerProfile) profile).getBankInfo(),
                        ((OwnerProfile) profile).getBrandName(),
                        ((OwnerProfile) profile).getExplanation()
                );

                break;
            case CUSTOMER:
                profile = session.createQuery("from CustomerProfile where userId = :userId", CustomerProfile.class).setParameter("userId", userId).getSingleResult();
                profileDTO = new ProfileDTO(
                        profile.getFullName(),
                        profile.getPhoneNumber(),
                        profile.getAddress(),
                        profile.getImageUrl(),
                        user.getEmail()
                );
                break;
            case COURTIER:
                profile = session.createQuery("from CourtierProfile where userId = :userId", CourtierProfile.class).setParameter("userId", userId).getSingleResult();
                profileDTO = new CourtierProfileDTO(
                        profile.getFullName(),
                        profile.getPhoneNumber(),
                        profile.getAddress(),
                        profile.getImageUrl(),
                        user.getEmail(),
                        ((CourtierProfile) profile).getBankInfo(),
                        ((CourtierProfile) profile).getStatus()
                );
                break;
        }
        return profileDTO;

    }
    public boolean isValidEmail(String email) {
        return UserHttpHandler.isValidEmail(email);
    }

}
