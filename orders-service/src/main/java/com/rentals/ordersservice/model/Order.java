package com.rentals.ordersservice.model;

import org.springframework.data.annotation.Id;

public class Order {
    @Id
    private String id;

    private String sellerId;
    private String renterId;
    private String productId;
    private String status;

    public Order() { }

    public Order(String sellerId, String renterId, String productId, String status) {
        this.sellerId = sellerId;
        this.renterId = renterId;
        this.productId = productId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getRenterId() {
        return renterId;
    }

    public void setRenterId(String renterId) {
        this.renterId = renterId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
