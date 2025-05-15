package com.models;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Order {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private Long courierId;
    private ArrayList<OrderItem> items;
    private String address;
    private double totalPrice;
    private OrderStatus status;
    private LocalDateTime dateTime;
    private ArrayList<OrderLogStatus> logStatuses;
}
