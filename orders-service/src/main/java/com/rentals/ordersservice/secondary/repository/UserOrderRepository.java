package com.rentals.ordersservice.secondary.repository;

import com.rentals.ordersservice.model.UserOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserOrderRepository extends MongoRepository<UserOrder, String> {
}
