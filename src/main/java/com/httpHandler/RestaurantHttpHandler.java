package com.httpHandler;

import com.controller.FoodItemController;
import com.controller.MenuController;
import com.google.gson.*;
import com.models.FoodItem;
import com.models.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.util.HttpUtil.sendResponse;



/**
 * /restaurants/{id}/item:
 *     parameters:
 *     - name: id
 *       in: path
 *       required: true
 *       schema:
 *         type: integer
 *     post:
 *       tags:
 *       - restaurant
 *       summary: Add food item to restaurant with a specific id
 *       security:
 *       - bearerAuth: []
 *       requestBody:
 *         required: true
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               required:
 *               - name
 *               - description
 *               - price
 *               - supply
 *               - categories
 *               - vendor_id
 *               properties:
 *                 name:
 *                   type: string
 *                 imageBase64:
 *                   type: string
 *                 description:
 *                   type: string
 *                 price:
 *                   type: integer
 *                 supply:
 *                   type: integer
 *                 categories:
 *                   type: array
 *                   minItems: 1
 *                   items:
 *                     type: string
 *       responses:
 *         '200':
 *           description: Food item created and added to restaurant successfully
 *           content:
 *             application/json:
 *               schema:
 *                 $ref: '#/components/schemas/food_item'
 *         '400':
 *           $ref: '#/components/responses/invalid_input'
 *         '401':
 *           $ref: '#/components/responses/unauthorized'
 *         '403':
 *           $ref: '#/components/responses/forbidden'
 *         '404':
 *           $ref: '#/components/responses/not_found'
 *         '409':
 *           $ref: '#/components/responses/conflict'
 *         '415':
 *           $ref: '#/components/responses/unsupported_media_type'
 *         '429':
 *           $ref: '#/components/responses/too_many_requests'
 *         '500':
 *           $ref: '#/components/responses/internal_server_error'
 */
public class RestaurantHttpHandler implements HttpHandler {
    private final FoodItemController foodItemController= new FoodItemController();
    private final MenuController menuController= new MenuController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
         String path = exchange.getRequestURI().getPath();
         String[] pathParts = path.split("/");
         Long restaurantId = null;

         if(pathParts.length < 2) {
             sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
             return;
         }
         try{
              restaurantId = Long.parseLong(pathParts[1]);
         } catch (NumberFormatException e) {
            sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
            return;
         }
         if(pathParts.length ==3 && pathParts[2].equals("item")) {
             String method = exchange.getRequestMethod();
             if(method.equals("POST")) {
                 handleAddItem(exchange,restaurantId);
             }else{
                 sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");
                 return;
             }
         }



    }
    private void handleAddItem(HttpExchange exchange,Long restaurantId) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);

        if (id == null) {
            sendResponse(exchange, 401, "Unauthorized request");
            return;
        }
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject foodItem = new Gson().fromJson(reader, JsonObject.class);
        FoodItem foodItemToAdd = new FoodItem();
        try{
            foodItemToAdd.setName(foodItem.get("name").getAsString());
        }catch (IllegalStateException  | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid name\"\n}");
            return;
        }
        try{
            foodItemToAdd.setDescription(foodItem.get("description").getAsString());
        }catch( IllegalStateException | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid description\"\n}");
            return;
        }
        try{
            foodItemToAdd.setPrice(foodItem.get("price").getAsInt());
        }catch( IllegalStateException | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid price\"\n}");
        }
        try{
            foodItemToAdd.setSupply(foodItem.get("supply").getAsInt());
        }catch( IllegalStateException | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid supply\"\n}");
        }
        try{
            foodItemToAdd.setVendorId(foodItem.get("vendorId").getAsLong());
        }catch(IllegalStateException | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid vendor_id\"\n}");
        }
        try{
            JsonArray categoriesArray = foodItem.getAsJsonArray("categories");
            List<String> categories = new ArrayList<>();
            for (JsonElement element : categoriesArray) {
                categories.add(element.getAsString());
            }
            foodItemToAdd.setCategories(categories);
        }catch(IllegalStateException | NullPointerException e) {
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid categories\"\n}");
        }
        if(foodItem.has("imageBase64")) {
            try{
                foodItemToAdd.setImageBase64(foodItem.get("imageBase64").getAsString());
            }catch(IllegalStateException e) {
                sendResponse(exchange, 400, "{\n  \"error\":\"Invalid imageBase64\"\n}");
            }
        }
        String response = foodItemController.addItem(foodItemToAdd,Long.parseLong(id),restaurantId);

        if(response.contains("not found")) {
            sendResponse(exchange, 404, response);
        }else if(response.contains("Invalid")){
            sendResponse(exchange, 400, response);
        }else if(response.contains("Internal server error")) {
            sendResponse(exchange, 500, response);
        }else if(response.contains("successfully")) {
            sendResponse(exchange, 200, new Gson().toJson(foodItemToAdd));
        }





    }
}
