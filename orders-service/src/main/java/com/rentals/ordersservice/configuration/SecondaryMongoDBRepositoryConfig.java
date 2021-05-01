package com.rentals.ordersservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.rentals.ordersservice.secondary.repository",
        mongoTemplateRef = "secondaryMongoTemplate"
)
public class SecondaryMongoDBRepositoryConfig extends AbstractMongoClientConfiguration {
    @Override
    protected String getDatabaseName() {
        return "user_orders_db";
    }
}
