package com.rentals.ordersservice.controller;

import com.rentals.ordersservice.model.Order;
import com.rentals.ordersservice.model.Product;
import com.rentals.ordersservice.model.Request;
import com.rentals.ordersservice.model.User;
import com.rentals.ordersservice.repository.OrderRepository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RestTemplate restTemplate;

    private String productServiceBaseURL = "http://products-service/";
    private String userServiceBaseURL = "http://users-service/";

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Order> createOrder(Order order) {
        try {
            Order newOrder = orderRepository.save(new Order(order.getSellerId(), order.getRenterId(), order.getProductId(), "ACTIVE", "PENDING", Instant.now().getEpochSecond()));
            return new ResponseEntity<Order>(newOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getSentOrders")
    public ResponseEntity<List<Request>> getSentRequests(@RequestParam("renterId") String renterId) {
        String productServiceURL = productServiceBaseURL + "getById";
        String userServiceURL = userServiceBaseURL + "getById";
        try {
            List<Order> ordersList = orderRepository.findByRenterIdAndStatus(renterId, "ACTIVE", Sort.by(Sort.Direction.ASC, "timestamp"));
            List<Request> requestList = new ArrayList<>();
            for (Order order : ordersList) {
                String id = order.getId();
                String status = order.getUserStatus();
                UriComponentsBuilder b1 = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", order.getProductId());
                UriComponentsBuilder b2 = UriComponentsBuilder.fromHttpUrl(userServiceURL).queryParam("id", order.getSellerId());
                Product product = restTemplate.getForObject(b1.toUriString(), Product.class);
                User user = restTemplate.getForObject(b2.toUriString(), User.class);
                Request req = new Request(id, user, product, status);
                requestList.add(req);
            }
            return new ResponseEntity<>(requestList, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getReceivedOrders")
    public ResponseEntity<List<Order>> getReceivedRequests(@RequestParam("sellerId") String sellerId) {
        List<Order> ordersList = orderRepository.findBySellerIdAndStatus(sellerId, "ACTIVE", Sort.by(Sort.Direction.ASC, "timestamp"));
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
