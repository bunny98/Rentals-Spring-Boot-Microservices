package com.rentals.ordersservice.redis.repository;

import com.rentals.ordersservice.model.ProductOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
@RequiredArgsConstructor
public class ProductOrderRedisRepositoryImpl implements ProductOrderRedisRepository {
    private static final String REDIS_ENTITY = "ProductOrder";
    @Autowired
    private RedisTemplate<String, ProductOrder> redisTemplate;

    private HashOperations<String, String, ProductOrder> hashOperations;

    @PostConstruct
    private void init(){
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public ProductOrder findProductOrderById(String productId) {
        return hashOperations.get(REDIS_ENTITY, productId);
    }

    @Override
    public void deleteProductOrderById(String productId) {
        hashOperations.delete(REDIS_ENTITY, productId);
    }

    @Override
    public void updateProductOrderById(ProductOrder productOrder) {
        hashOperations.put(REDIS_ENTITY, productOrder.getProductId(), productOrder);
    }

    @Override
    public void saveProductOrderById(ProductOrder productOrder) {
        hashOperations.put(REDIS_ENTITY, productOrder.getProductId(), productOrder);
    }

    @Override
    public Long getSize() {
        return hashOperations.size(REDIS_ENTITY);
    }

    @Override
    public boolean isPresent(String id) {
        return hashOperations.hasKey(REDIS_ENTITY, id);
    }
}
