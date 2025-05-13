package com.models;

import java.time.LocalDateTime;

public class OrderLogStatus {
    private long orderId;
    private long logId;
    private OrderStatus status;
    private LocalDateTime changeAt;
}
