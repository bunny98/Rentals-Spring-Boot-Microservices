package com.rentals.collegeservice.repository;

import com.rentals.collegeservice.model.College;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CollegeRepository extends MongoRepository<College, String> {
}
