package com.controller;

import com.DAO.UserDAO;
import com.google.gson.JsonObject;
import com.models.BankInfo;
import com.models.User;



public class UserController {
    private UserDAO userDAO = new UserDAO();

    public JsonObject addUser(User user){
        JsonObject response = new JsonObject();
        if(!isValidNumber(user.getPhone())){
            response.addProperty("error", "Invalid phone");
            return response;
        }
        if( user.getEmail() != null && !isValidEmail(user.getEmail())){
            response.addProperty("error", "Invalid email");
            return response;
        }
        return userDAO.addUser(user);

    }
    public JsonObject logInUser(String phone, String password){
        JsonObject response = new JsonObject();
        if(!isValidNumber(phone)){
            response.addProperty("error", "Invalid phone");
            return response;
        }
        return userDAO.logInUser(phone,password);
    }
    public JsonObject updateProfile(JsonObject input, Long id){
        JsonObject response = new JsonObject();
        if(input.has("full_name")){
            String fullName = input.get("full_name").getAsString();
            if(fullName.isEmpty()){
                response.addProperty("error", "Invalid full_name");
                return response;
            }
        }
        if(input.has("phone")){
            String phone = input.get("phone").getAsString();
            if(!isValidNumber(phone)){
                response.addProperty("error", "Invalid phone");
                return response;
            }
        }
        if(input.has("email")){
            String email = input.get("email").getAsString();
            if(!isValidEmail(email)){
                response.addProperty("error", "Invalid email");
                return response;
            }
        }
        if(input.has("address")){
            String address = input.get("address").getAsString();
            if(address.isEmpty()){
                response.addProperty("error", "Invalid address");
                return response;
            }
        }
        if(input.has("bank_info")){
              JsonObject bankInfo= input.get("bank_info").getAsJsonObject();
              if(bankInfo.has("bank_name")){
                  String bankName = bankInfo.get("bank_name").getAsString();
                  if(bankName.isEmpty()){
                      response.addProperty("error", "Invalid bank_name");
                      return response;
                  }
              }
              if(bankInfo.has("account_number")){
                  String accountNumber = bankInfo.get("account_number").getAsString();
                  if(accountNumber.isEmpty()){
                      response.addProperty("error", "Invalid account_number");
                      return response;
                  }
              }
        }
        return userDAO.updateUser(input,id);
    }
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }


        String[] parts = email.split("@");
        if (parts.length < 2) {
            return false;
        }

        String localPart = parts[0];
        String domainPart = parts[1];


        if (localPart.isEmpty() || domainPart.isEmpty()) {
            return false;
        }


        if (!domainPart.contains(".")) {
            return false;
        }else{
            String[] domainParts = domainPart.split("\\.");
            if(domainParts.length < 2){
                return false;
            }
        }
        return true;

    }

    /**
     *
     * input format for phone number : [user input example : 09194772344]
     */
    public static boolean isValidNumber(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        } else if (number.length() == 11) {
            try {
                Double.parseDouble(number);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }else if (number.equals("admin")){
            return true;
        }
        else {
            return false;

        }
    }
}
