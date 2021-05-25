package com.rentals.ordersservice.redis.repository;

import com.rentals.ordersservice.model.ProductOrder;

public interface ProductOrderRedisRepository {
    ProductOrder findProductOrderById(String productId);
    void deleteProductOrderById(String productId);
    void updateProductOrderById(ProductOrder productOrder);
    void saveProductOrderById(ProductOrder productOrder);
    Long getSize();
    boolean isPresent(String id);
}
