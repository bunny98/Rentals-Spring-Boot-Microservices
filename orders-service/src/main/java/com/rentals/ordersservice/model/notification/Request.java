package com.rentals.ordersservice.model.notification;

import com.rentals.ordersservice.model.Product;
import com.rentals.ordersservice.model.User;

public class Request {
    private final String id;
    private final Product product;
    private final User recipientUser;
    private final String status;

    public Request(String id, Product product, User recipientUser, String status) {
        this.id = id;
        this.product = product;
        this.recipientUser = recipientUser;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public String getStatus() {
        return status;
    }
}
