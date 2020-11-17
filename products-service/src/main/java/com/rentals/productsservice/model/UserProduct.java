package com.rentals.productsservice.model;

public class UserProduct {
    private Product product;
    private String status;

    public UserProduct(Product product, String status) {
        this.product = product;
        this.status = status;
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
