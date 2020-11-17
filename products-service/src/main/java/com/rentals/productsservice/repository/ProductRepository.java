package com.rentals.productsservice.repository;

import com.rentals.productsservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCollegeIdAndStatus(String collegeId, String status);
    List<Product> findBySellerIdAndStatus(String sellerId, String status);
}
