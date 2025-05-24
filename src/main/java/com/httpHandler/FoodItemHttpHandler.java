package com.httpHandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class FoodItemHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
         exchange.sendResponseHeaders(200, 0);
    }
}
