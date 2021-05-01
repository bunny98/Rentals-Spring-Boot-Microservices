package com.rentals.ordersservice.controller;

import com.rentals.ordersservice.model.*;
import com.rentals.ordersservice.model.notification.Notification;
import com.rentals.ordersservice.model.notification.Request;
import com.rentals.ordersservice.primary.repository.ProductOrderRepository;

import java.io.IOException;
import java.util.*;

import com.rentals.ordersservice.secondary.repository.UserOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    private ProductOrderRepository productOrderRepository;
    @Autowired
    private UserOrderRepository userOrderRepository;
    @Autowired
    private RestTemplate restTemplate;

    private final String productServiceBaseURL = "http://products-service/product/";
    private final String userServiceBaseURL = "http://users-service/user/";
    private Map<String, SseEmitter> emitters = new HashMap<>();
    private int count = 0;

    @CrossOrigin
    @RequestMapping(value = "/subscribe", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribe(@RequestParam("userId") String userId) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, sseEmitter);

        sendNotificationEventTo(userId);

        sseEmitter.onCompletion(() -> emitters.remove(userId));
        sseEmitter.onError((e) -> emitters.remove(userId));
        sseEmitter.onTimeout(() -> emitters.remove(userId));

        return sseEmitter;
    }

    @GetMapping("/sendMessage")
    public ResponseEntity sendMessage(@RequestParam("userId") String userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                count++;
                emitter.send(SseEmitter.event().name("NOTIFICATION").data("EVENT " + count));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    public void sendNotificationEventTo(String userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                List<Request> sentReq = getSentRequests(userId).getBody();
                List<Request> receivedReq = getReceivedRequests(userId).getBody();
                Notification notification = new Notification(sentReq, receivedReq);
                emitter.send(SseEmitter.event().name("NOTIFICATION").data(notification));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String generateId(){
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        return uuidAsString;
    }

    public void saveUserOrder(String userId, String productId, boolean isSeller){
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(userId);
        UserOrder userOrder;
        if(userOrderOptional.isPresent()){
            userOrder = userOrderOptional.get();
            List<String> productIds = isSeller ? userOrder.getReceivedReqProductIds() : userOrder.getSentReqProductIds();
            if(!productIds.contains(productId)){
                productIds.add(0, productId);
            }
            if(isSeller){
                userOrder.setReceivedReqProductIds(productIds);
            }else
                userOrder.setSentReqProductIds(productIds);
        }else {
            if(isSeller)
                userOrder = new UserOrder(userId, new ArrayList<>(), new ArrayList<>(Arrays.asList(productId)));
            else
                userOrder = new UserOrder(userId, new ArrayList<>(Arrays.asList(productId)), new ArrayList<>());
        }
        userOrderRepository.save(userOrder);
    }

    public Product getProductById(String productId) {
        try {
            String productServiceURL = productServiceBaseURL + "getById";
            String productUri = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", productId).toUriString();
            Product product = restTemplate.getForObject(productUri, Product.class);
            return product;
        } catch (Exception e) {
            return null;
        }
    }

    public User getUserById(String userId) {
        try {
            String userServiceURL = userServiceBaseURL + "getById";
            UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(userServiceURL).queryParam("id", userId);
            User user = restTemplate.getForObject(uri.toUriString(), User.class);
            return user;
        } catch (Exception e) {
            System.out.println("CANNOT FETCH USER " + userId);
            return null;
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ProductRequest> createOrder(@Valid CreateOrder createOrderObj) {
        System.out.println(createOrderObj.toString());
        if (createOrderObj.getSellerId().equals(createOrderObj.getRenterId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<ProductOrder> productOrderOptional = productOrderRepository.findById(createOrderObj.getProductId());
        ProductOrder productOrder;
        if(productOrderOptional.isEmpty()){
            User seller = getUserById(createOrderObj.getSellerId());
            Product product = getProductById(createOrderObj.getProductId());
            if(seller != null && product != null && product.getSellerId().equals(createOrderObj.getSellerId())){
                productOrder = new ProductOrder(createOrderObj.getProductId(), product, seller, ProductOrderStatus.ACTIVE,  new ArrayList<>());
            }else
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            productOrder = productOrderOptional.get();
            if(productOrder.getStatus().equals(ProductOrderStatus.INACTIVE) || !productOrder.getSeller().getId().equals(createOrderObj.getSellerId()))
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            List<ProductRequest> receivedRequestList = productOrder.getReceivedRequests();
            for(ProductRequest req: receivedRequestList){
                if(req.getRenter().getId().equals(createOrderObj.getRenterId()) && req.getStatus().equals(RequestStatus.PENDING))
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
        User renter = getUserById(createOrderObj.getRenterId());
        if(renter!=null){
            ProductRequest newProductRequest = new ProductRequest(generateId(), renter, RequestStatus.PENDING);
            productOrder.getReceivedRequests().add(0, newProductRequest);
            productOrderRepository.save(productOrder);

            saveUserOrder(createOrderObj.getSellerId(), createOrderObj.getProductId(), true);
            saveUserOrder(createOrderObj.getRenterId(), createOrderObj.getProductId(), false);

            sendNotificationEventTo(createOrderObj.getSellerId());
            sendNotificationEventTo(createOrderObj.getRenterId());

            return  new ResponseEntity<>(newProductRequest, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getProductOrderById")
    public ResponseEntity<ProductOrder> getProductOrderById(@RequestParam("productId") String productId){
        Optional<ProductOrder> productOrderData = productOrderRepository.findById(productId);
        if(productOrderData.isPresent()){
            return new ResponseEntity<>(productOrderData.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getUserOrderProductIds")
    public ResponseEntity<UserOrder> getUserOrderById(@RequestParam("userId") String userId){
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(userId);
        if(userOrderOptional.isPresent()){
            return new ResponseEntity<>(userOrderOptional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getAllProductOrders")
    public ResponseEntity<List<ProductOrder>> getAllProductOrders() {
        List<ProductOrder> orders = productOrderRepository.findAll();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/getSentRequests")
    public ResponseEntity<List<Request>> getSentRequests(@RequestParam("renterId") String renterId) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(renterId);
        List<Request> sentRequestsList = new ArrayList<>();
        if(userOrderOptional.isPresent()){
            UserOrder userOrder = userOrderOptional.get();
            List<String> productIds = userOrder.getSentReqProductIds();
            for(String productId : productIds) {
                Optional<ProductOrder> productOrderOptional = productOrderRepository.findById(productId);
                if (productOrderOptional.isPresent()) {
                    ProductOrder productOrder = productOrderOptional.get();
                    List<ProductRequest> productRequests = productOrder.getReceivedRequests();
                    for (ProductRequest req : productRequests) {
                        if (req.getRenter().getId().equals(renterId)) {
                            Request sentRequest = new Request(req.getId(), productOrder.getProduct(), productOrder.getSeller(), req.getStatus());
                            sentRequestsList.add(sentRequest);
                        }
                    }
                }
            }
        }
        return new ResponseEntity<>(sentRequestsList, HttpStatus.OK);
    }

    @GetMapping("/getReceivedRequests")
    public ResponseEntity<List<Request>> getReceivedRequests(@RequestParam("sellerId") String sellerId) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(sellerId);
        List<Request> receivedRequestsList = new ArrayList<>();
        if(userOrderOptional.isPresent()){
            List<String> productsIds = userOrderOptional.get().getReceivedReqProductIds();
            for(String prodId : productsIds){
                Optional<ProductOrder> productOrderOptional = productOrderRepository.findById(prodId);
                if(productOrderOptional.isPresent()){
                    List<ProductRequest> productRequestList = productOrderOptional.get().getReceivedRequests();
                    for(ProductRequest productRequest : productRequestList){
                        if(!productRequest.getStatus().equals(RequestStatus.DENIED)) {
                            Request receivedReq = new Request(productRequest.getId(), productOrderOptional.get().getProduct(), productRequest.getRenter(), productRequest.getStatus());
                            receivedRequestsList.add(receivedReq);
                        }
                    }
                }
            }
        }
        return new ResponseEntity<>(receivedRequestsList, HttpStatus.OK);
    }

    @PatchMapping("/markProductOrderInactive")
    public ResponseEntity<ProductOrder> markProductOrderInactive(@RequestParam("productId") String id) {
        Optional<ProductOrder> orderData = productOrderRepository.findById(id);
        if (orderData.isPresent()) {
            ProductOrder order = orderData.get();
            order.setStatus(ProductOrderStatus.INACTIVE);
            return new ResponseEntity<>(productOrderRepository.save(order), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
//
    @PatchMapping("/acceptProductOrder")
    public ResponseEntity markProductOrderAccepted(@RequestParam("productId") String productId, @RequestParam("requestId") String requestId) {
        Optional<ProductOrder> productOrderOptional = productOrderRepository.findByProductIdAndStatus(productId, ProductOrderStatus.ACTIVE);
        if(productOrderOptional.isPresent()){
            ProductOrder productOrder = productOrderOptional.get();
            List<ProductRequest> receivedRequestList = productOrder.getReceivedRequests();
            boolean isValidReq = false;
            for(ProductRequest req: receivedRequestList){
                if(req.getId().equals(requestId) && req.getStatus().equals(RequestStatus.PENDING)){
                    req.setStatus(RequestStatus.ACCEPTED);
                    isValidReq = true;
                }else{
                    req.setStatus(RequestStatus.DENIED);
                }
            }
            if(isValidReq){
                productOrder.setReceivedRequests(receivedRequestList);
                productOrder.setStatus(ProductOrderStatus.INACTIVE);
                productOrderRepository.save(productOrder);

                String productServiceURL = productServiceBaseURL + "markProductInactive";
                String productUri = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", productId).toUriString();
                Product product = restTemplate.patchForObject(productUri, Void.class,Product.class);

                for(ProductRequest productRequest : productOrder.getReceivedRequests()){
                    sendNotificationEventTo(productRequest.getRenter().getId());
                }
                sendNotificationEventTo(productOrder.getSeller().getId());

                return new ResponseEntity(HttpStatus.OK);
            }
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PatchMapping("/denyProductOrder")
    public ResponseEntity markProductOrderDenied(@RequestParam("productId") String productId, @RequestParam("requestId") String requestId) {
        Optional<ProductOrder> productOrderOptional = productOrderRepository.findByProductIdAndStatus(productId, ProductOrderStatus.ACTIVE);
        if(productOrderOptional.isPresent()){
            ProductOrder productOrder = productOrderOptional.get();
            List<ProductRequest> receivedRequestList = productOrder.getReceivedRequests();
            ProductRequest productRequest = null;
            for(ProductRequest req: receivedRequestList){
                if(req.getId().equals(requestId) && req.getStatus().equals(RequestStatus.PENDING)) {
                    req.setStatus(RequestStatus.DENIED);
                    productRequest = req;
                    break;
                }
            }
            if(productRequest != null){
                productOrder.setReceivedRequests(receivedRequestList);
                productOrderRepository.save(productOrder);

                sendNotificationEventTo(productRequest.getRenter().getId());
                sendNotificationEventTo(productOrder.getSeller().getId());

                return new ResponseEntity(HttpStatus.OK);
            }
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getProductIdsForPendingRequestsByUser")
    public ResponseEntity<List<String>> getProductIdsForPendingRequestsByUser(@RequestParam("userId") String userId){
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(userId);
        List<String> ret = new ArrayList<>();
        if(userOrderOptional.isPresent()){
            List<String> sentOrderProdIds = userOrderOptional.get().getSentReqProductIds();
            for(String prodId : sentOrderProdIds){
                Optional<ProductOrder> productOrderOptional = productOrderRepository.findByProductIdAndStatus(prodId, ProductOrderStatus.ACTIVE);
                if(productOrderOptional.isPresent()){
                    for(ProductRequest req : productOrderOptional.get().getReceivedRequests()){
                        if(req.getRenter().getId().equals(userId) && req.getStatus().equals(RequestStatus.PENDING))
                            ret.add(productOrderOptional.get().getProductId());
                    }
                }
            }
        }
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProductOrder")
    public ResponseEntity deleteProductOrder(@RequestParam("productId") String productId) {
        if (productOrderRepository.existsById(productId)) {
            productOrderRepository.deleteById(productId);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}
