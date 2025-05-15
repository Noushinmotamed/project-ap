package com.models;

import java.time.LocalDateTime;

public class OrderLogStatus {
    private Long orderId;
    private Long logId;
    private OrderStatus status;
    private LocalDateTime changeAt;
}
