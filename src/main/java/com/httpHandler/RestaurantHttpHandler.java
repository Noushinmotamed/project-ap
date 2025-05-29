package com.httpHandler;

import com.controller.FoodItemController;
import com.controller.MenuController;
import com.google.gson.*;
import com.models.FoodItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.util.HttpUtil.sendResponse;



///restaurants/{id}/item/{item_id}:
//parameters:
//        - name: id
//in: path
//required: true
//schema:
//type: integer
//    - name: item_id
//in: path
//required: true
//schema:
//type: integer
//put:
//tags:
//        - restaurant
//summary: Edit food item of a restaurant with a specific id
//security:
//        - bearerAuth: []
//requestBody:
//required: true
//content:
//application/json:
//schema:
//type: object
//properties:
//name:
//type: string
//imageBase64:
//type: string
//description:
//type: string
//price:
//type: integer
//supply:
//type: integer
//keywords:
//type: array
//minItems: 1
//items:
//type: string
//responses:
//        '200':
//description: Food item edited successfully
//content:
//application/json:
//schema:
//$ref: '#/components/schemas/food_item'
//        '400':
//$ref: '#/components/responses/invalid_input'
//        '403':
//$ref: '#/components/responses/forbidden'
//        '401':
//$ref: '#/components/responses/unauthorized'
//        '404':
//$ref: '#/components/responses/not_found'
//        '409':
//$ref: '#/components/responses/conflict'
//        '415':
//$ref: '#/components/responses/unsupported_media_type'
//        '429':
//$ref: '#/components/responses/too_many_requests'
//        '500':
//$ref: '#/components/responses/internal_server_error'
//delete:
//tags:
//        - restaurant
//summary: Delete item
//security:
//        - bearerAuth: []
//responses:
//        '200':
//description: Food item removed successfully
//content:
//application/json:
//schema:
//$ref: '#/components/schemas/message'
//        '400':
//$ref: '#/components/responses/invalid_input'
//        '403':
//$ref: '#/components/responses/forbidden'
//        '401':
//$ref: '#/components/responses/unauthorized'
//        '404':
//$ref: '#/components/responses/not_found'
//        '409':
//$ref: '#/components/responses/conflict'
//        '415':
//$ref: '#/components/responses/unsupported_media_type'
//        '429':
//$ref: '#/components/responses/too_many_requests'
//        '500':
//$ref: '#/components/responses/internal_server_error'
public class RestaurantHttpHandler implements HttpHandler {
    private final FoodItemController foodItemController= new FoodItemController();
    private final MenuController menuController= new MenuController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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
                 handleAddItem(exchange,restaurantId);
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
                 handleEditItem(exchange,itemId,restaurantId);
             }else if(method.equals("DELETE")) {
                 handleDeleteItem(exchange,itemId,restaurantId);
             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
             }
         }



    }
    private void handleAddItem(HttpExchange exchange,Long restaurantId) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);

        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
            return;
        }

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
        System.out.println(foodItemToAdd.getKeywords());
        if(foodItem.has("imageBase64")) {
            try{
                foodItemToAdd.setImageBase64(foodItem.get("imageBase64").getAsString());
            }catch(IllegalStateException | NullPointerException  | ClassCastException | NumberFormatException e) {
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid imageBase64\"\n}");
                e.printStackTrace();
                return;
            }
        }
        JsonObject response = foodItemController.addItem(foodItemToAdd,Long.parseLong(id),restaurantId);
        String responseString = new GsonBuilder().setPrettyPrinting().create().toJson(response);


        if(response.has("error")) {
            String error = response.get("error").getAsString();
            if(error.contains("not found")) {
                sendResponse(exchange, 404, responseString);
            }else if(error.contains("Invalid")){
                sendResponse(exchange, 400, responseString);
            }else if(error.contains("Internal server error")) {
                sendResponse(exchange, 500, responseString);
            }else if(error.contains("Forbidden")) {
                sendResponse(exchange, 403,responseString);
            }

        }else{
            sendResponse(exchange, 200, responseString);
        }





    }
    private void handleEditItem(HttpExchange exchange,Long itemId, Long restaurantId) throws IOException {
            String token = exchange.getRequestHeaders().getFirst("Authorization");
            String id = JwtUtil.validateToken(token);
            if (id == null) {
                sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
                return;
            }
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
            JsonObject response = foodItemController.editItem(fooditem,Long.parseLong(id),restaurantId,itemId);


        String responseString = new GsonBuilder().setPrettyPrinting().create().toJson(response);
            System.out.println(responseString);

        if(response.has("error")) {
                String error = response.get("error").getAsString();
                if(error.contains("not found")) {
                    sendResponse(exchange, 404, responseString);
                }else if(error.contains("Invalid")){
                    sendResponse(exchange, 400, responseString);
                }else if(error.contains("Internal server error")) {
                    sendResponse(exchange, 500, responseString);
                }else if(error.contains("Forbidden")) {
                    sendResponse(exchange, 403,responseString);
                }

            }else{
                sendResponse(exchange, 200, responseString);
            }
            }







    private void handleDeleteItem(HttpExchange exchange,Long itemId, Long restaurantId) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);

        if (id == null) {
            sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
            return;
        }
        System.out.println("res :" + restaurantId);
        System.out.println("id :" + id);
        System.out.println("item: "+ itemId);
        JsonObject response = foodItemController.deleteItem(Long.parseLong(id),restaurantId,itemId);
        String responseString = new GsonBuilder().setPrettyPrinting().create().toJson(response);
        if(response.has("error")) {
            String error = response.get("error").getAsString();
            if(error.contains("not found")) {
                sendResponse(exchange, 404, responseString);
            }else if(error.contains("Invalid")){
                sendResponse(exchange, 400, responseString);
            }else if(error.contains("Internal server error")) {
                sendResponse(exchange, 500, responseString);
            }else if(error.contains("Forbidden")) {
                sendResponse(exchange, 403,responseString);
            }

        }else{
            sendResponse(exchange, 200, responseString);
        }
    }
}
