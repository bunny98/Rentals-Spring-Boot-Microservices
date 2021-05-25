package com.rentals.ordersservice.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;


public class ProductOrder implements Serializable {
    @Id
    private String productId;
    private Product product;
    private User seller;
    private String status;
    private List<ProductRequest> receivedProductRequests;

    public ProductOrder() {
    }

    public ProductOrder(String productId, Product product, User seller, String status, List<ProductRequest> receivedProductRequests) {
        this.productId = productId;
        this.product = product;
        this.seller = seller;
        this.status = status;
        this.receivedProductRequests = receivedProductRequests;
    }

    @Override
    public String toString() {
        return "ProductOrder{" +
                "productId='" + productId + '\'' +
                ", product=" + product +
                ", seller=" + seller +
                ", status='" + status + '\'' +
                ", receivedProductRequests=" + receivedProductRequests +
                '}';
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ProductRequest> getReceivedRequests() {
        return receivedProductRequests;
    }

    public void setReceivedRequests(List<ProductRequest> receivedProductRequests) {
        this.receivedProductRequests = receivedProductRequests;
    }
}
