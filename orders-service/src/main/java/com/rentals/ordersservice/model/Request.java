package com.rentals.ordersservice.model;

public class Request {
    private String id;
    private User recipientUser;
    private Product product;
    private String status;

    public Request(String id, User recipientUser, Product product, String status) {
        this.id = id;
        this.recipientUser = recipientUser;
        this.product = product;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
