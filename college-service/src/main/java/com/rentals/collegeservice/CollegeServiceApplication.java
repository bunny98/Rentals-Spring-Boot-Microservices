package com.rentals.collegeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class CollegeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollegeServiceApplication.class, args);
	}

}
