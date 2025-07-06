package com.DAO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.models.*;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Objects;

import static com.util.JwtUtil.generateToken;                           // DO I USE "PHONE NUMBER ALREADY EXISTS" OR "INVALID PHONE" ????
                                                                         // WHAT DO I RETURN IF COURIER HASN'T BEEN CHECKED MY ADMIN YET?! (IS COURIERsTATUS IS NULL)
public class UserDAO {
    public JsonObject addUser(User user) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            if(isPhoneNumberTaken(session,user.getPhone())){
                response.addProperty("error", "Phone number already exists");
                return response;
            }
            session.save(user);
            if(user.getRole() == Role.buyer){
                Basket basket = new Basket(user.getId());
                Wallet wallet = new Wallet(user.getId());
                session.save(basket);
                session.save(wallet);
                response.add("wallet",gson.toJsonTree(wallet));             //for client
            }
            tx.commit();
            String token = generateToken(user.getId().toString());
            response.addProperty("message", "User registered successfully");
            response.addProperty("user_id", user.getId());
            response.addProperty("token", token);
            return response;

        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject logInUser(String phone, String password) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            User user =session.createQuery("from User where phone = :phoneNumber", User.class).setParameter("phoneNumber", phone).uniqueResult();
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(!user.getPassword().equals(password)){
                response.addProperty("error", "Invalid password");
                return response;
            }
            if((user.getRole() == Role.courier || user.getRole() == Role.seller) && (user.getLoginStatus()==null || user.getLoginStatus() != CourierStatus.approved)){
                response.addProperty("error", "Forbidden request");
                response.addProperty("hint", "Courier not yet approved");
                return response;
            }
            Wallet wallet = session.createQuery("from Wallet where userId = :userId", Wallet.class).setParameter("userId", user.getId()).uniqueResult();  //for client
            if(wallet == null){
                wallet = new Wallet(user.getId());
                session.save(wallet);
            }
            String token = generateToken(user.getId().toString());
            response.addProperty("token", token);
            response.addProperty("message", "User logged in successfully");
            response.add("user",gson.toJsonTree(user));
            response.add("wallet",gson.toJsonTree(wallet));
            return response;


        }catch(Exception e){
            response.addProperty("error", "Internal server error");
            e.printStackTrace();
            return response;
        }
    }
    public JsonObject getUser(Long id) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            return gson.toJsonTree(user).getAsJsonObject();

        } catch (Exception e) {
            response.addProperty("error", "Internal server error");
            return response;
        }
    }
    public JsonObject updateUser(JsonObject input,Long id) {
        JsonObject response = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user == null){
                response.addProperty("error", "Resource not found");
                return response;
            }
            if(input.has("phone")){
                User user1 =session.createQuery("from User where phone = :phoneNumber", User.class).setParameter("phoneNumber",input.get("phone").getAsString()).uniqueResult();
                if(user1!= null && !Objects.equals(user1.getId(), id)){
                    response.addProperty("error", "Phone number already exists");
                    return response;
                }else{
                    user.setPhone(input.get("phone").getAsString());
                }
            }
            if(input.has("full_name")){
                user.setFullName(input.get("full_name").getAsString());
            }
            if(input.has("email")){
                user.setEmail(input.get("email").getAsString());
            }
            if(input.has("address")){
                user.setAddress(input.get("address").getAsString());
            }
            if(input.has("profileImageBase64")){
                user.setImageBase64(input.get("profileImageBase64").getAsString());
            }
            if(input.has("bank_info")){
               JsonObject bankInfo = input.get("bank_info").getAsJsonObject();
               if(user.getBankInfo()==null){
                   BankInfo newbankInfo = new BankInfo();
                   if(!bankInfo.has("bank_name")){
                       response.addProperty("error", "Invalid bank_name");
                       return response;
                   }else if(!bankInfo.has("account_number")){
                       response.addProperty("error", "Invalid account_number");
                       return response;
                   }else{
                       newbankInfo.setAccountNumber(bankInfo.get("account_number").getAsString());
                       newbankInfo.setBankName(bankInfo.get("bank_name").getAsString());
                       user.setBankInfo(newbankInfo);

                   }
               }else{
                   if(bankInfo.has("account_number")){
                       user.getBankInfo().setAccountNumber(bankInfo.get("account_number").getAsString());
                   }
                   if(bankInfo.has("bank_name")){
                       user.getBankInfo().setBankName(bankInfo.get("bank_name").getAsString());
                   }
               }
            }
            session.update(user);
            tx.commit();
            response.addProperty("message", "Profile updated successfully");
            response.add("user",gson.toJsonTree(user));
            return response;
        }

    }

    private boolean isPhoneNumberTaken(Session session, String phoneNumber){
        User user = session.createQuery("from User where phone = :phoneNumber", User.class).setParameter("phoneNumber", phoneNumber).uniqueResult();
        return user!=null ;


    }
}
