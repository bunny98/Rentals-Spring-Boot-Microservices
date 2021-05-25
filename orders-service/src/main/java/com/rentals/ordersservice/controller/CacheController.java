package com.rentals.ordersservice.controller;

import com.rentals.ordersservice.model.ProductOrder;
import com.rentals.ordersservice.primary.repository.ProductOrderRepository;
import com.rentals.ordersservice.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/cache")
public class CacheController {
    @Autowired
    private ProductOrderRepository productOrderRepository;
    @Autowired
    private CacheService cacheService;
    private final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    @GetMapping("/checkCache")
    public ResponseEntity checkCache(@RequestParam("id") String productId){
        ProductOrder productOrder = cacheService.getCachedProductOrder(productId);
        if(productOrder == null){
            return new ResponseEntity("NOT FOUND!!", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(productOrder, HttpStatus.OK);
    }

    @GetMapping("/putInCache")
    public ResponseEntity putInCache(@RequestParam("id") String productId){
        ProductOrder productOrder = productOrderRepository.findById(productId).get();
        cacheService.cacheProductOrder(productOrder);
        return new ResponseEntity(productOrder, HttpStatus.OK);
    }

    @GetMapping("/cacheSize")
    public ResponseEntity cacheSize(){
        Long size = cacheService.getCacheSize();
        return new ResponseEntity(size, HttpStatus.OK);
    }
}
