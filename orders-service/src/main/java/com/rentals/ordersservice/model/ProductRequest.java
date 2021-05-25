package com.rentals.ordersservice.model;

import java.io.Serializable;

public class ProductRequest implements Serializable {
    private final String id;
    private final User renter;
    private String status;

    public ProductRequest(String id, User renter, String status) {
        this.id = id;
        this.renter = renter;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public User getRenter() {
        return renter;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ProductRequest{" +
                "id='" + id + '\'' +
                ", renter=" + renter +
                ", status='" + status + '\'' +
                '}';
    }
}
