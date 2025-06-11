package com.httpHandler;

import com.controller.FoodItemController;
import com.controller.MenuController;
import com.controller.RestaurantOrderController;
import com.google.gson.*;
import com.models.FoodItem;
import com.models.Menu;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.HttpUtil;
import com.util.JwtUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.httpHandler.AdminHttpHandler.parseQueryParameters;
import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;


                                                                                   // if something in path has the wrong format, Invalid or Not found ?

public class RestaurantHttpHandler implements HttpHandler {
    private final FoodItemController foodItemController= new FoodItemController();
    private final MenuController menuController= new MenuController();
    private final RestaurantOrderController restaurantOrderController= new RestaurantOrderController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
            return;
        }
         String path = exchange.getRequestURI().getPath();
         String[] pathParts = path.split("/");
         Long restaurantId = null;

         if(pathParts.length < 3) {
             sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
             System.out.println("path len less than 3");
             return;
         }
         try{
              restaurantId = Long.parseLong(pathParts[2]);
         } catch (NumberFormatException e) {
            sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
             System.out.println("restaurant id invalid in path");
            return;
         }
         if(pathParts.length ==4 && pathParts[3].equals("item")) {
             String method = exchange.getRequestMethod();
             if(method.equals("POST")) {
                 handleAddItem(exchange,restaurantId,Long.parseLong(id));
             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                 return;
             }
         }else if(pathParts.length ==5 && pathParts[3].equals("item")) {
             Long itemId = null ;
             try{
                 itemId = Long.parseLong(pathParts[4]);
             }catch (NumberFormatException e){
                 sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
                 return;
             }
             String method = exchange.getRequestMethod();
             if(method.equals("PUT")) {
                 handleEditItem(exchange,itemId,restaurantId,Long.parseLong(id));
             }else if(method.equals("DELETE")) {
                 handleDeleteItem(exchange,itemId,restaurantId,Long.parseLong(id));
             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                 return;
             }
         }else if(pathParts.length ==4 && pathParts[3].equals("menu")) {
             String method = exchange.getRequestMethod();
             if(method.equals("POST")) {
                 handleAddMenu(exchange,restaurantId,Long.parseLong(id));
             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                 return;
             }
        }else if(pathParts.length ==5 && pathParts[3].equals("menu")) {
             String titleEncoded = pathParts[4];
             String title = titleEncoded.replace("%20", " ");
             String method = exchange.getRequestMethod();
             if(method.equals("DELETE")) {
                 handleDeleteMenu(exchange,restaurantId,title,Long.parseLong(id));
             }else if(method.equals("PUT")) {
                 handleAddOItemToMenu(exchange,restaurantId,title,Long.parseLong(id));
             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                 return;
             }
        }else if(pathParts.length ==6 && pathParts[3].equals("menu")) {
            String titleEncoded = pathParts[4];
            String title = titleEncoded.replace("%20", " ");
            Long itemId = null;
            try{
                itemId = Long.parseLong(pathParts[5]);
            }catch (NumberFormatException e){
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
                return;
            }
            String method = exchange.getRequestMethod();
            if(method.equals("DELETE")) {
                handleRemoveItemFromMenu(exchange,restaurantId,title,itemId,Long.parseLong(id));
            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                return;
            }

        }else if( pathParts.length ==4 && pathParts[3].equals("orders")) {
             String method = exchange.getRequestMethod();
             if(method.equals("GET")) {
                 handleGetOrders(exchange,restaurantId,Long.parseLong(id));

             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                 return;
             }
         }



    }
    private void handleAddItem(HttpExchange exchange,Long restaurantId,Long id) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject foodItem = null;
        try {
             foodItem = new Gson().fromJson(reader, JsonObject.class);
        }catch (JsonSyntaxException e){
            sendResponse(exchange, 400, "Invalid json format");
            return;
        }
        FoodItem foodItemToAdd = new FoodItem();
        try{
            foodItemToAdd.setName(foodItem.get("name").getAsString());
        }catch (IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid name\"\n}");
            return;
        }
        try{
            foodItemToAdd.setDescription(foodItem.get("description").getAsString());
        }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid description\"\n}");
            return;
        }
        try{
            System.out.println(foodItem.get("price"));
            foodItemToAdd.setPrice(foodItem.get("price").getAsInt());
        }catch( IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid price\"\n}");
            System.out.println("price invalid");

            return;
        }
        try{
            foodItemToAdd.setSupply(foodItem.get("supply").getAsInt());
        }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid supply\"\n}");
            e.printStackTrace();
            return;

        }
        try{
            foodItemToAdd.setVendorId(foodItem.get("vendor_id").getAsLong());
        }catch(IllegalStateException | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid vendor_id\"\n}");
            e.printStackTrace();
            return;
        }
        try{
            JsonArray keywordsArray = foodItem.getAsJsonArray("keywords");
            List<String> keywords = new ArrayList<>();
            for (JsonElement element : keywordsArray) {
                keywords.add(element.getAsString());
            }
            foodItemToAdd.setKeywords(keywords);
        }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid keywords\"\n}");
            e.printStackTrace();
            return;
        }

        if(foodItem.has("imageBase64")) {
            try{
                foodItemToAdd.setImageBase64(foodItem.get("imageBase64").getAsString());
            }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid imageBase64\"\n}");
                e.printStackTrace();
                return;
            }
        }
        JsonObject response = foodItemController.addItem(foodItemToAdd,id,restaurantId);
         handleResponse(exchange,response);

    }
    private void handleEditItem(HttpExchange exchange,Long itemId, Long restaurantId,Long id) throws IOException {

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            FoodItem fooditem = new FoodItem();
            JsonObject newItem = null;
            try {
               newItem = new Gson().fromJson(reader, JsonObject.class);
            }catch (JsonSyntaxException e){
                e.printStackTrace();
                sendResponse(exchange, 400, "Invalid json format");
                return;
            }
            if(newItem.has("name")) {
                fooditem.setName(newItem.get("name").getAsString());
            }
            if(newItem.has("description")) {
                fooditem.setDescription(newItem.get("description").getAsString());
            }
            if(newItem.has("imageBase64")) {
                fooditem.setImageBase64(newItem.get("imageBase64").getAsString());
            }
            if(newItem.has("price")) {
                try{
                    fooditem.setPrice(newItem.get("price").getAsInt());
                }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
                    e.printStackTrace();
                    sendResponse(exchange, 400, "{\n  \"error\":\"Invalid price\"\n}");
                    return;
                }
            }
            if(newItem.has("supply")) {
                try{
                    fooditem.setSupply(newItem.get("supply").getAsInt());
                }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
                    e.printStackTrace();
                    sendResponse(exchange, 400, "{\n  \"error\":\"Invalid supply\"\n}");
                    return;
                }
            }
            if(newItem.has("keywords")&& newItem.get("keywords").isJsonArray()
            ) {
                try{
                    JsonArray keywordsArray = newItem.getAsJsonArray("keywords");
                    List<String> keywords = new ArrayList<>();
                    for (JsonElement element : keywordsArray) {
                        keywords.add(element.getAsString());
                    }
                    fooditem.setKeywords(keywords);
                }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
                    sendResponse(exchange, 400, "{\n  \"error\":\"Invalid keywords\"\n}");
                    e.printStackTrace();
                    return;
                }
            }
            JsonObject response = foodItemController.editItem(fooditem,id,restaurantId,itemId);
            handleResponse(exchange,response);

            }

    private void handleDeleteItem(HttpExchange exchange,Long itemId, Long restaurantId,Long id) throws IOException {

        JsonObject response = foodItemController.deleteItem(id,restaurantId,itemId);
        handleResponse(exchange,response);

    }
    private void handleAddMenu(HttpExchange exchange, Long restaurantId,Long id) throws IOException {

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject menu = null;
        try {
            menu = new Gson().fromJson(reader, JsonObject.class);
        }catch (JsonSyntaxException e){
            sendResponse(exchange, 400, "Invalid json format");
            return;
        }
        Menu menuItem = new Menu();
        if(menu.has("title")) {
            menuItem.setTitle(menu.get("title").getAsString());
        }else{
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid title\"\n}");
            return;
        }

        JsonObject response = menuController.addMenu(menuItem,id,restaurantId);
        handleResponse(exchange,response);


    }
    private void handleDeleteMenu(HttpExchange exchange, Long restaurantId, String title,Long id) throws IOException {

        JsonObject response = menuController.deleteMenu(id,restaurantId,title);
        handleResponse(exchange,response);


    }
    private void handleAddOItemToMenu(HttpExchange exchange, Long restaurantId, String title,Long id) throws IOException {
         Long item_id = null ;
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject object = null;
        try {
            object = new Gson().fromJson(reader, JsonObject.class);
        }catch (JsonSyntaxException e){
            sendResponse(exchange, 400, "Invalid json format");
            return;
        }
        if(object.has("item_id")) {
            try{
                 item_id = object.get("item_id").getAsLong();
            }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e){
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid item_id\"\n}");
                return;
            }
        }else{
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid item_id\"\n}");
            return;
        }
        reader.close();
        JsonObject response = menuController.addItem(id,restaurantId,item_id,title);
        handleResponse(exchange,response);

    }
    private void handleRemoveItemFromMenu(HttpExchange exchange, Long restaurantId, String title, Long itemId,Long id) throws IOException {

        JsonObject response = menuController.deleteItem(id,restaurantId,title,itemId);
        handleResponse(exchange,response);



    }//status,search,user,courier
    private void handleGetOrders(HttpExchange exchange, Long restaurantId, Long id) throws IOException {
        Map<String, String> queryParams = parseQueryParameters(exchange.getRequestURI().getQuery());
        String status = queryParams.get("status");
        String search = queryParams.get("search");
        String user = queryParams.get("user");
        String courier = queryParams.get("courier");
        JsonElement response = restaurantOrderController.getOrders(status,search,user,courier,restaurantId,id);
        handleResponse(exchange,response);

    }

}
