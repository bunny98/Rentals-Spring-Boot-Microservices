package com.rentals.productsservice.model;

import javax.validation.constraints.NotNull;
import java.util.List;

public class UserProduct {
    private String id;
    private String name;
    private int price;
    private List<String> contentURLs;
    private String sellerId;
    private String status;

    public UserProduct(String id, String name, int price, List<String> contentURLs, String sellerId, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.contentURLs = contentURLs;
        this.sellerId = sellerId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<String> getContentURLs() {
        return contentURLs;
    }

    public void setContentURLs(List<String> contentURLs) {
        this.contentURLs = contentURLs;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
