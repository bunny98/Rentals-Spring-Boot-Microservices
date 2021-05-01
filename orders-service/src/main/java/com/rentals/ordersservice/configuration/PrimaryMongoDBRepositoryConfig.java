package com.rentals.ordersservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.rentals.ordersservice.primary.repository",
        mongoTemplateRef = "primaryMongoTemplate"
)
public class PrimaryMongoDBRepositoryConfig extends AbstractMongoClientConfiguration{

    @Override
    protected String getDatabaseName() {
        return "orders_db";
    }
}
