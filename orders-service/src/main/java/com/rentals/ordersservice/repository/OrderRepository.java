package com.rentals.ordersservice.repository;

import com.rentals.ordersservice.model.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByRenterIdAndStatus(String renterId, String status, Sort sort);
    List<Order> findBySellerIdAndStatus(String sellerId, String status, Sort sort);
    List<Order> findByProductIdAndStatus(String productId, String status);
}
