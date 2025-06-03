package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
//order:
//type: object
//required: [id, delivery_address, customer_id, item_ids, vendor_id, raw_price, tax_fee, courier_fee, additional_fee, pay_price, status, created_at, updated_at]
//properties:



//enum:
//        - submitted
//          - unpaid and cancelled
//          - waiting vendor
//          - cancelled
//          - finding courier
//          - on the way
//          - completed

//required:
//        - id
//      - item_id
//      - rating
//      - comment
//      - user_id
//      - created_at
//properties:
//id:
//type: integer
//item_id:
//type: integer
//rating:
//type: integer
//comment:
//type: string
//imageBase64:
//type: array
//items:
//type: string
//user_id:
//type: integer
//created_at:
//type: string                       // ADD NULLABLE = FALSE
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SerializedName("id")
    @Column
    private Long orderId;

    @Column
    @SerializedName("user_id")
    private Long userId;

    @Column
    @SerializedName("vendor_id")
    private Long vendorId;

    @Column
    private Long sellerId;

    @Column
    private Long courierId;

    @ElementCollection
    @CollectionTable(
            name = "orderItemList",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @Column
    @SerializedName("item_ids")
    private Set<Long> itemIds = new HashSet<>();

    @Column
    @SerializedName("delivery_address")
    private String deliveryAddress;

    @Column
    @SerializedName("raw_price")
    private Integer rawPrice;

    @Column
    @SerializedName("tax_fee")
    private Integer taxFee;

    @Column
    @SerializedName("additional_fee")
    private Integer additionalFee=0;

    @Column
    @SerializedName("courier_fee")
    private Integer courierFee=0;

    @Column
    @SerializedName("pay_price")
    private Integer payPrice=0;

    @Enumerated(EnumType.STRING)
    @Column
    private OrderStatus status;


    @SerializedName("created_at")
    @Column
    private String createdAt;

    @Column
    @SerializedName("updated_at")
    private String updatedAt;


    @Column
    private String imageBase64;

    @Column
    private String comment;

    @Column
    private int rating;



    public Long getUserId() {
        return userId;
    }
    public void setStatus( OrderStatus status ) {
        this.status = status;
    }
    public int getRawPrice() {
        return rawPrice;
    }
    public Long getSellerId(){
        return sellerId;
    }
    public OrderStatus getStatus(){
        return status;
    }
    public Set<Long> getItemIds() {
        return itemIds;
    }

}
