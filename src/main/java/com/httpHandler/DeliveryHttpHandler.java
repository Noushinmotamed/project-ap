package com.httpHandler;

import com.DAO.DeliveryDAO;
import com.controller.DeliveryController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.util.JwtUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.httpHandler.AdminHttpHandler.parseQueryParameters;
import static com.util.HttpUtil.handleResponse;
import static com.util.HttpUtil.sendResponse;


/**
 * endpoints : /deliveries/available
 *            /deliveries/history
 *            /deliveries/{order_id}
 */
                                                                          // I ASSUME HERE FOR DELIVERIES/AVAILABLE I SHOULD RETURN THE ORDERS WITH STATUS FINDING COURIER
public class DeliveryHttpHandler implements HttpHandler {
    private final DeliveryController deliveryController = new DeliveryController();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        String id = JwtUtil.validateToken(token);
        if(id == null) {
                sendResponse(exchange, 401, "{\n  \"error\":\"Unauthorized request\"\n}");
                return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if(path.equals("/deliveries/available")){
            if(method.equals("GET")){
                handleGetOrders(exchange,id);
            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");

            }

        }else if(path.equals("/deliveries/history")){
            if(method.equals("GET")){
                handleGetOrdersHistory(exchange,id);
            }else{
                sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");

            }
        }else{
            String[] pathParts = path.split("/");
            Long orderId = null;
            if(pathParts.length == 3 && pathParts[1].equals("deliveries") ){
                try{
                    orderId = Long.parseLong(pathParts[2]);
                }catch(NumberFormatException e){
                    sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");
                    return;
                }
                if(method.equals("PATCH")) {
                    handleChangeOrderStatus(exchange,orderId,id);
                }else{
                    sendResponse(exchange, 405, "{\n  \"error\":\"Method not allowed\"\n}");

                }
            }else{
                sendResponse(exchange, 404, "{\n  \"error\":\"Resource not found\"\n}");

            }
        }

    }

    /**
     * this method allows courier to get the list of all orders with the status finding courier
     *
     * @param id courierId
     * @throws IOException not handled
     */
    private void handleGetOrders(HttpExchange exchange, String id) throws IOException {
        JsonElement response = deliveryDAO.getOrders(id);
        handleResponse(exchange,response);

    }

    /**
     * this method allows a courier to see the deliveries they have made, with optional filter in query
     *  queries : search , vendor, user.
     *  response : list of orders.
     *
     * @param id courier's id
     * @throws IOException not handled
     */
    private void handleGetOrdersHistory(HttpExchange exchange, String id) throws IOException {
        Map<String, String> queryParams = parseQueryParameters(exchange.getRequestURI().getQuery());
        String search = queryParams.get("search");
        String vendor = queryParams.get("vendor");
        String user = queryParams.get("user");
        JsonElement response = deliveryController.getOrdersHistory(search,vendor,user,Long.parseLong(id));
        handleResponse(exchange,response);


    }

    /**
     * after courier saw the available orders to deliver (finding courier), this method allows them to choose an available order
     * to deliver, then set their status. order's orderStatus, orderDeliveryStatus and courierId and updatedAt will be updated.
     *
     * @param orderId  id of order
     * @param id       id of courier
     * @throws IOException not handled in the method
     */
    public void handleChangeOrderStatus(HttpExchange exchange, Long orderId, String id) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        JsonObject body = new Gson().fromJson(reader, JsonObject.class);
        String status = null;
        if(body.has("status")){
            status = body.get("status").getAsString();
        }else{
            sendResponse(exchange, 400, "{\n  \"error\":\"Invalid status\"\n}");
            return;
        }
        reader.close();
        JsonObject response = deliveryController.changeOrderStatus(status,orderId,Long.parseLong(id));
        handleResponse(exchange,response);

    }

}
