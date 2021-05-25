package com.rentals.ordersservice.services;


import com.rentals.ordersservice.controller.OrderController;
import com.rentals.ordersservice.model.ProductOrder;
import com.rentals.ordersservice.redis.repository.ProductOrderRedisRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("CacheService")
public class CacheService {
    @Autowired
    private ProductOrderRedisRepositoryImpl productOrderRedisRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    public ProductOrder getCachedProductOrder(String id) {
        ProductOrder productOrder = null;
        try {
            productOrder = productOrderRedisRepository.findProductOrderById(id);
            LOGGER.info("GET CACHED PRODUCT ORDER ID: {}", id);
        } catch (Exception e) {
            LOGGER.warn("No product order was yet cached with the given id : {}", id);
        }
        return productOrder;
    }

    public void cacheProductOrder(ProductOrder productOrder) {
        try {
            productOrderRedisRepository.saveProductOrderById(productOrder);
            LOGGER.info("CACHED PRODUCT ORDER ID: {}", productOrder.getProductId());
        } catch (Exception e) {
            LOGGER.error("Unable to store the product order : {}, EXCEPTION: {}", productOrder.getProductId(), e.getMessage());
        }
    }

    public void updateCachedProductOrder(ProductOrder productOrder){
        try {
            productOrderRedisRepository.updateProductOrderById(productOrder);
            LOGGER.info("UPDATED CACHED PRODUCT ORDER ID: {}", productOrder.getProductId());
        } catch (Exception e) {
            LOGGER.error("Unable to update the product order : {}, EXCEPTION: {}", productOrder.getProductId(), e.getMessage());
        }
    }

    public Long getCacheSize(){
        Long ret = Long.MAX_VALUE;
        try {
            ret = productOrderRedisRepository.getSize();
        } catch (Exception e) {
            LOGGER.error("Unable to get cache size!!!");
        }
        return ret;
    }
}
