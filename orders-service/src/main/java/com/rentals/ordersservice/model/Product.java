package com.rentals.ordersservice.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    @Id
    private String id;

    private String name;
    private String sellerId;
    private String collegeId;
    private String status;
    private int price;
    private List<String> contentURLs;

    public Product() {
    }

    public Product(String name, String sellerId, String collegeId, String status, int price, List<String> contentURLs) {
        this.name = name;
        this.sellerId = sellerId;
        this.collegeId = collegeId;
        this.status = status;
        this.price = price;
        this.contentURLs = contentURLs;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", collegeId='" + collegeId + '\'' +
                ", status='" + status + '\'' +
                ", price=" + price +
                ", contentURLs=" + contentURLs +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(String collegeId) {
        this.collegeId = collegeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
