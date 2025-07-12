package com.controller;

import com.DAO.MySearchDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class MySearchController {
    private final MySearchDAO mySearchDAO = new MySearchDAO();
    public JsonElement getItems(JsonObject input, Long id){
          JsonObject response = new JsonObject();
          int price=-1;
          String search=null;
          ArrayList<String> keywords = new ArrayList<>();
          if(input.has("price")){
              try{
                   price = Integer.parseInt(input.get("price").getAsString());
                  if(price <0){
                      response.addProperty("error", "Invalid price");
                      return response;
                  }
              }catch(NumberFormatException e){
                  response.addProperty("error", "Invalid price");
                  return response;
              }
          }
          if(input.has("search")){
              search = input.get("search").getAsString();
          }
          if(input.has("keywords")){
              try{
                  JsonArray Jsonkeywords = input.get("keywords").getAsJsonArray();
                  for(JsonElement Jsonkeyword : Jsonkeywords){
                      keywords.add(Jsonkeyword.getAsString());
                  }

              }catch(Exception e){
                  response.addProperty("error", "Invalid keywords");
                  return response;
              }
          }
          return mySearchDAO.getItems(id,search,price,keywords);
    }
    public JsonElement getVendors(JsonObject input, Long id){
        JsonObject response = new JsonObject();
        String search=null;
        ArrayList<String> keywords = new ArrayList<>();
        if(input.has("search")){
            search = input.get("search").getAsString();
        }
        if(input.has("keywords")){
            try{
                JsonArray Jsonkeywords = input.get("keywords").getAsJsonArray();
                for(JsonElement Jsonkeyword : Jsonkeywords){
                    keywords.add(Jsonkeyword.getAsString());
                }

            }catch(Exception e){
                response.addProperty("error", "Invalid keywords");
                return response;
            }
        }
        return mySearchDAO.getVendors(id,search,keywords);
    }
}
