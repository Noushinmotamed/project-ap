package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

//    transaction:
//      type: object
//      required:
//      - id
//      - order_id
//      - user_id
//      - method
//      - status
//      properties:
//        id:
//          type: integer
//        order_id:
//          type: integer
//        user_id:
//          type: integer
//        method:
//          type: string
//          enum:
//          - wallet
//          - online
//        status:
//          type: string
//          enum:
//          - success
//          - failed

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @SerializedName("user_id")
    private Long userId;

    @Column
    @SerializedName("order_id")
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column
    private TransactionStatus status;


    private String message;   //REMOVE IT

    public void setMessage(String message) {
        this.message = message;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getUserId() {
        return userId;
    }
    public String getMessage() {
        return message;
    }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public Long getOrderId() {
        return orderId;
    }

}
