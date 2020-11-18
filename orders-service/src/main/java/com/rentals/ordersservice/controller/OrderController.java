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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RestTemplate restTemplate;

    private final String productServiceBaseURL = "http://products-service/product/";
    private final String userServiceBaseURL = "http://users-service/user/";

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Order> createOrder(Order order) {
        String productServiceURL = productServiceBaseURL + "getStatus";
        String uri = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", order.getProductId()).toUriString();
        String status = restTemplate.getForObject(uri, String.class);
        assert status != null;
        if(status.equals("ACTIVE")){
            try {
                Order newOrder = orderRepository.save(new Order(order.getSellerId(), order.getRenterId(), order.getProductId(), "ACTIVE", "PENDING", Instant.now().getEpochSecond()));
                return new ResponseEntity<Order>(newOrder, HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);

    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Order>> getAllOrders(){
        List<Order> orders = orderRepository.findAll();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/getSentRequests")
    public ResponseEntity<List<Request>> getSentRequests(@RequestParam("renterId") String renterId) {
        try {
            List<Order> ordersList = orderRepository.findByRenterIdAndStatus(renterId, "ACTIVE", Sort.by(Sort.Direction.DESC, "timestamp"));
            List<Request> requestList = generateRequestList(ordersList, true);
            return new ResponseEntity<>(requestList, HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getReceivedRequests")
    public ResponseEntity<List<Request>> getReceivedRequests(@RequestParam("sellerId") String sellerId) {
        try {
            List<Order> ordersList = orderRepository.findBySellerIdAndStatus(sellerId, "ACTIVE", Sort.by(Sort.Direction.DESC, "timestamp"));
            List<Request> requestList = generateRequestList(ordersList, false);
            return new ResponseEntity<>(requestList, HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/markOrderInactive")
    public ResponseEntity<Order> markOrderInactive(@RequestParam("id") String id) {
        Optional<Order> orderData = orderRepository.findById(id);
        if (orderData.isPresent()) {
            Order order = orderData.get();
            order.setStatus("INACTIVE");
            return new ResponseEntity<>(orderRepository.save(order), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/accept")
    public ResponseEntity markOrderAccepted(@RequestParam("id") String id) {
        String productServiceURL = productServiceBaseURL + "markProductInactive";
        Optional<Order> orderData = orderRepository.findById(id);
        if (orderData.isPresent()) {
            Order order = orderData.get();
            List<Order> otherOrders = orderRepository.findByProductIdAndStatus(order.getProductId(), order.getStatus());
            for (Order ord : otherOrders) {
                if (ord.getId().equals(order.getId())) {
                    ord.setUserStatus("ACCEPTED");

                    try{
                        String uri = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", ord.getProductId()).toUriString();
                        restTemplate.exchange(uri, HttpMethod.PATCH, HttpEntity.EMPTY, Product.class);
                    }catch (Exception e) {
                        System.out.println(e.getMessage());
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                } else {
                    ord.setUserStatus("DENIED");
                }
            }
            orderRepository.saveAll(otherOrders);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/deny")
    public ResponseEntity<Order> markOrderDenied(@RequestParam("id") String id) {
        Optional<Order> orderData = orderRepository.findById(id);
        if (orderData.isPresent()) {
            Order order = orderData.get();
            order.setUserStatus("DENIED");
            return new ResponseEntity<>(orderRepository.save(order), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteOrder(@RequestParam("id") String id){
        if(orderRepository.existsById(id)){
            orderRepository.deleteById(id);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    private List<Request> generateRequestList(List<Order> ordersList, boolean fetchSeller){
        String productServiceURL = productServiceBaseURL + "getById";
        String userServiceURL = userServiceBaseURL + "getById";
        List<Request> requestList = new ArrayList<>();
        for (Order order : ordersList) {
            String id = order.getId();
            String status = order.getUserStatus();
            String userId = fetchSeller ? order.getSellerId() : order.getRenterId();
            UriComponentsBuilder b1 = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", order.getProductId());
            UriComponentsBuilder b2 = UriComponentsBuilder.fromHttpUrl(userServiceURL).queryParam("id", userId);
            Product product = restTemplate.getForObject(b1.toUriString(), Product.class);
            User user = restTemplate.getForObject(b2.toUriString(), User.class);
            Request req = new Request(id, user, product, status);
            requestList.add(req);
        }
        return requestList;
    }

}
