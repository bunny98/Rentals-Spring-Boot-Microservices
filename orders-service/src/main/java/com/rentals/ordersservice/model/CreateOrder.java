package com.rentals.ordersservice.model;

import javax.validation.constraints.NotNull;

public class CreateOrder {
    @NotNull
    private String sellerId;
    @NotNull
    private String renterId;
    @NotNull
    private String productId;

    public CreateOrder() {


    }

    public CreateOrder(String sellerId, String renterId, String productId) {
        this.sellerId = sellerId;
        this.renterId = renterId;
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "CreateOrder{" +
                "sellerId='" + sellerId + '\'' +
                ", renterId='" + renterId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getRenterId() {
        return renterId;
    }

    public String getProductId() {
        return productId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setRenterId(String renterId) {
        this.renterId = renterId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
