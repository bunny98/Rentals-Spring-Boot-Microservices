package com.rentals.ordersservice.controller;

import com.rentals.ordersservice.model.Order;
import com.rentals.ordersservice.repository.OrderRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Order> createOrder(Order order) {
        try {
            Order newOrder = orderRepository.save(new Order(order.getSellerId(), order.getRenterId(), order.getProductId(), "ACTIVE", "PENDING"));
            return new ResponseEntity<Order>(newOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getSentOrders")
    public ResponseEntity<List<Order>> getSentRequests(@RequestParam("renterId") String renterId) {
        List<Order> ordersList = orderRepository.findByRenterIdAndStatus(renterId, "ACTIVE");
        return new ResponseEntity<>(ordersList, HttpStatus.OK);
    }

    @GetMapping("/getReceivedOrders")
    public ResponseEntity<List<Order>> getReceivedRequests(@RequestParam("sellerId") String sellerId) {
        List<Order> ordersList = orderRepository.findBySellerIdAndStatus(sellerId, "ACTIVE");
        return new ResponseEntity<>(ordersList, HttpStatus.OK);
    }

    @PutMapping("/markOrderInactive")
    public ResponseEntity<Order> markOrderInactive(@RequestParam("id") String id) {
        Optional<Order> orderData = orderRepository.findById(id);
        if (orderData.isPresent()) {
            Order order = orderData.get();
            order.setStatus("INACTIVE");
            return new ResponseEntity<>(orderRepository.save(order), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/accept")
    public ResponseEntity markOrderAccepted(@RequestParam("id") String id) {
        Optional<Order> orderData = orderRepository.findById(id);
        if (orderData.isPresent()) {
            Order order = orderData.get();
            List<Order> otherOrders = orderRepository.findByProductIdAndStatus(order.getProductId(), order.getStatus());
            for (Order ord : otherOrders) {
                if (ord.getId().equals(order.getId())) {
                    ord.setUserStatus("ACCEPTED");
                } else {
                    ord.setUserStatus("DENIED");
                }
            }
            orderRepository.saveAll(otherOrders);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/deny")
    public ResponseEntity<Order> markOrderDenied(@RequestParam("id") String id) {
        Optional<Order> orderData = orderRepository.findById(id);
        if (orderData.isPresent()) {
            Order order = orderData.get();
            order.setUserStatus("DENIED");
            return new ResponseEntity<>(orderRepository.save(order), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

}
