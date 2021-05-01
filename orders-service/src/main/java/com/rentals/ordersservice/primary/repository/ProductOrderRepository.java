package com.rentals.ordersservice.primary.repository;

import com.rentals.ordersservice.model.ProductOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOrderRepository extends MongoRepository<ProductOrder, String> {
    Optional<ProductOrder> findByProductIdAndStatus(String id, String status);
}
