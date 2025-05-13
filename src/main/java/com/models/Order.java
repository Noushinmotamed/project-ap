package com.models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Order {
    private long orderId;
    private long userId;
    private long restaurantId;
    private ArrayList<OrderItem> items;
    private String address;
    private double totalPrice;
    private OrderStatus status;
    private LocalDateTime dateTime;
    private ArrayList<OrderLogStatus> logStatuses;
}
