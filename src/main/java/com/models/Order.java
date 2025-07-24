package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
//order:
//type: object
//required: [id, delivery_address, customer_id, item_ids, vendor_id, raw_price, tax_fee, courier_fee, additional_fee, pay_price, status, created_at, updated_at]
//properties:


//id:
//type: integer
//delivery_address:
//type: string
//customer_id:
//type: integer
//vendor_id:
//type: integer
//coupon_id:
//type: integer
//item_ids:
//type: array
//items:
//type: integer
//raw_price:
//type: integer
//tax_fee:
//type: integer
//additional_fee:
//type: integer
//courier_fee:
//type: integer
//pay_price:
//type: integer
//courier_id:
//type: integer
//status:
//type: string
//enum:
//        - submitted
//          - unpaid and cancelled
//          - waiting vendor
//          - cancelled
//          - finding courier
//          - on the way
//          - completed
//created_at:
//type: string
//updated_at:
//type: string                      // ADD NULLABLE = FALSE
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SerializedName("id")
    @Column
    private Long orderId;

    @Column
    @SerializedName("customer_id")
    private Long customerId;

    @Column
    @SerializedName("vendor_id")
    private Long vendorId;

    @Column
    private Long sellerId;

    @Column
    @SerializedName("courier_id")
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
    private Integer taxFee=0;

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
    private OrderDeliveryStatus deliveryStatus;
    @Column
    private OrderVendorStatus vendorStatus;

    @Column
    private String userName;

    @Column(name = "quantity_data", columnDefinition = "TEXT")
    private String quantityData;

    @ElementCollection
    @CollectionTable(
            name = "orderItemNamesList",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @Column
    @SerializedName("item_names")
    private Set<String> itemNames = new HashSet<>();

    @Column
    @SerializedName("vendor_name")
    private String vendorName;




    public Long getCustomerId() {
        return customerId;
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
    public double getTotalPrice() {
        return payPrice==0 ? rawPrice + taxFee + additionalFee + courierFee : payPrice;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Long getVendorId() {
        return vendorId;
    }
    public OrderDeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }
    public void setDeliveryStatus(OrderDeliveryStatus deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setRawPrice(Integer rawPrice) { this.rawPrice = rawPrice; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setItemIds(Set<Long> itemIds) { this.itemIds = itemIds; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public void setTaxFee(Integer taxFee) { this.taxFee = taxFee; }
    public void setAdditionalFee(Integer additionalFee) { this.additionalFee = additionalFee; }

    public void setPayPrice(int payPrice) {
        this.payPrice = payPrice;
    }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt;
    }
   public Long getCourierId() { return courierId; }

    public OrderVendorStatus getVendorStatus() {
        return vendorStatus;
    }
    public void setVendorStatus(OrderVendorStatus vendorStatus) { this.vendorStatus = vendorStatus; }

    public void setUserName(String fullName) {
        this.userName = fullName;
    }

    public String getQuantityData() {
        return quantityData;
    }

    public void setQuantityData(String quantityData) {
        this.quantityData = quantityData;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public void setItemNames(Set<String> itemNames) { this.itemNames = itemNames; }

}
