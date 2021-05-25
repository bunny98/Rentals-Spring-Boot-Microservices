package com.rentals.ordersservice.controller;

import com.rentals.ordersservice.model.*;
import com.rentals.ordersservice.model.notification.Notification;
import com.rentals.ordersservice.model.notification.Request;
import com.rentals.ordersservice.primary.repository.ProductOrderRepository;

import java.io.IOException;
import java.util.*;

import com.rentals.ordersservice.secondary.repository.UserOrderRepository;
import com.rentals.ordersservice.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Autowired
    private CacheService cacheService;


    private final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

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
        LOGGER.info("SUBSCRIBED SSE USER: {}", userId);
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
                LOGGER.info("SENT NOTIFICATION EVENT TO : {}", userId);
            } catch (Exception e) {
                LOGGER.error("EXCEPTION SENDING NOTIFICATION EVENT TO {}\n EXCEPTION: {}", userId, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String generateId() {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        return uuidAsString;
    }

    public void saveUserOrder(String userId, String productId, boolean isSeller) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(userId);
        UserOrder userOrder;
        if (userOrderOptional.isPresent()) {
            userOrder = userOrderOptional.get();
            List<String> productIds = isSeller ? userOrder.getReceivedReqProductIds() : userOrder.getSentReqProductIds();
            if (!productIds.contains(productId)) {
                productIds.add(0, productId);
            }
            if (isSeller) {
                userOrder.setReceivedReqProductIds(productIds);
            } else
                userOrder.setSentReqProductIds(productIds);
        } else {
            if (isSeller)
                userOrder = new UserOrder(userId, new ArrayList<>(), new ArrayList<>(Arrays.asList(productId)));
            else
                userOrder = new UserOrder(userId, new ArrayList<>(Arrays.asList(productId)), new ArrayList<>());
        }
        userOrderRepository.save(userOrder);
        LOGGER.info("SAVED USER ORDER: {}", userOrder.toString());
    }

    public Product getProductById(String productId) {
        try {
            String productServiceURL = productServiceBaseURL + "getById";
            String productUri = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", productId).toUriString();
            Product product = restTemplate.getForObject(productUri, Product.class);
            LOGGER.info("GET PRODUCT BY ID : {}", product.toString());
            return product;
        } catch (Exception e) {
            LOGGER.error("EXCEPTION GETTING PRODUCT BY ID {}\n{}", productId, e.getMessage());
            return null;
        }
    }

    public User getUserById(String userId) {
        try {
            String userServiceURL = userServiceBaseURL + "getById";
            UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(userServiceURL).queryParam("id", userId);
            User user = restTemplate.getForObject(uri.toUriString(), User.class);
            LOGGER.info("GET USER BY ID : {}", user.toString());
            return user;
        } catch (Exception e) {
            LOGGER.error("EXCEPTION GETTING USER BY ID {}\n{}", userId, e.getMessage());
            return null;
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ProductRequest> createOrder(@Valid CreateOrder createOrderObj) {
        System.out.println(createOrderObj.toString());
        if (createOrderObj.getSellerId().equals(createOrderObj.getRenterId())) {
            LOGGER.warn("SELLER ID AND RENTER ID ARE SAME");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ProductOrder productOrder;
        try {
            productOrder = getProductOrderById(createOrderObj.getProductId()).getBody();
            if (productOrder.getStatus().equals(ProductOrderStatus.INACTIVE) || !productOrder.getSeller().getId().equals(createOrderObj.getSellerId())) {
                LOGGER.warn("Product Order is INACTIVE or WRONG SELLER ID");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            List<ProductRequest> receivedRequestList = productOrder.getReceivedRequests();
            for (ProductRequest req : receivedRequestList) {
                if (req.getRenter().getId().equals(createOrderObj.getRenterId()) && req.getStatus().equals(RequestStatus.PENDING)) {
                    LOGGER.warn("Renter has ALREADY made a request");
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            }
        }catch (Exception e){
            User seller = getUserById(createOrderObj.getSellerId());
            Product product = getProductById(createOrderObj.getProductId());
            if (seller != null && product != null && product.getSellerId().equals(createOrderObj.getSellerId())) {
                productOrder = new ProductOrder(createOrderObj.getProductId(), product, seller, ProductOrderStatus.ACTIVE, new ArrayList<>());
            } else {
                LOGGER.warn("SELLER DOESN'T EXIST OR PRODUCT DOESN'T EXIST OR WRONG SELLER ID");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            cacheService.cacheProductOrder(productOrder);
        }
        User renter = getUserById(createOrderObj.getRenterId());
        if (renter != null) {
            ProductRequest newProductRequest = new ProductRequest(generateId(), renter, RequestStatus.PENDING);
            productOrder.getReceivedRequests().add(0, newProductRequest);
            cacheService.updateCachedProductOrder(productOrder);
            productOrderRepository.save(productOrder);

            saveUserOrder(createOrderObj.getSellerId(), createOrderObj.getProductId(), true);
            saveUserOrder(createOrderObj.getRenterId(), createOrderObj.getProductId(), false);

            sendNotificationEventTo(createOrderObj.getSellerId());
            sendNotificationEventTo(createOrderObj.getRenterId());
            LOGGER.info("NEW PRODUCT ORDER CREATED \n{}", productOrder.toString());
            return new ResponseEntity<>(newProductRequest, HttpStatus.CREATED);
        }
        LOGGER.warn("WRONG RENTER ID");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getProductOrderById")
    public ResponseEntity<ProductOrder> getProductOrderById(@RequestParam("productId") String productId) {
        ProductOrder productOrder = cacheService.getCachedProductOrder(productId);
        if (productOrder != null) {
            LOGGER.info("PRODUCT ORDER BY ID FROM CACHE: {}", productOrder.toString());
            return new ResponseEntity<>(productOrder, HttpStatus.OK);
        }
        Optional<ProductOrder> productOrderData = productOrderRepository.findById(productId);
        if (productOrderData.isPresent()) {
            cacheService.cacheProductOrder(productOrderData.get());
            productOrder = productOrderData.get();
            LOGGER.info("PRODUCT ORDER BY ID FROM DB: {}", productOrder.toString());
            return new ResponseEntity<>(productOrder, HttpStatus.OK);
        }
        LOGGER.warn("PRODUCT ORDER NOT FOUND ID: {}", productId);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getUserOrderProductIds")
    public ResponseEntity<UserOrder> getUserOrderById(@RequestParam("userId") String userId) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(userId);
        if (userOrderOptional.isPresent()) {
            UserOrder userOrder = userOrderOptional.get();
            LOGGER.info("GET USER ORDER BY ID: {}", userOrder.toString());
            return new ResponseEntity<>(userOrder, HttpStatus.OK);
        }
        LOGGER.warn("USER ORDER NOT FOUND ID: {}", userId);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getAllProductOrders")
    public ResponseEntity<List<ProductOrder>> getAllProductOrders() {
        List<ProductOrder> orders = productOrderRepository.findAll();
        LOGGER.info("GET ALL PRODUCT ORDERS LENGTH: {}", orders.size());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/getSentRequests")
    public ResponseEntity<List<Request>> getSentRequests(@RequestParam("renterId") String renterId) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(renterId);
        List<Request> sentRequestsList = new ArrayList<>();
        if (userOrderOptional.isPresent()) {
            UserOrder userOrder = userOrderOptional.get();
            List<String> productIds = userOrder.getSentReqProductIds();
            for (String productId : productIds) {
                try {
                    ProductOrder productOrder = getProductOrderById(productId).getBody();
                    List<ProductRequest> productRequests = productOrder.getReceivedRequests();
                    for (ProductRequest req : productRequests) {
                        if (req.getRenter().getId().equals(renterId)) {
                            Request sentRequest = new Request(req.getId(), productOrder.getProduct(), productOrder.getSeller(), req.getStatus());
                            sentRequestsList.add(sentRequest);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("PRODUCT ORDER WITH ID: {} NOT FOUND INSIDE GET SENT REQ", productId);
                }
            }
        }
        LOGGER.info("GET SENT REQ FOR USER ID: {} LENGTH: {}", renterId, sentRequestsList.size());
        return new ResponseEntity<>(sentRequestsList, HttpStatus.OK);
    }

    @GetMapping("/getReceivedRequests")
    public ResponseEntity<List<Request>> getReceivedRequests(@RequestParam("sellerId") String sellerId) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(sellerId);
        List<Request> receivedRequestsList = new ArrayList<>();
        if (userOrderOptional.isPresent()) {
            List<String> productsIds = userOrderOptional.get().getReceivedReqProductIds();
            for (String prodId : productsIds) {
                try {
                    ProductOrder productOrder = getProductOrderById(prodId).getBody();
                    List<ProductRequest> productRequestList = productOrder.getReceivedRequests();
                    for (ProductRequest productRequest : productRequestList) {
                        if (!productRequest.getStatus().equals(RequestStatus.DENIED)) {
                            Request receivedReq = new Request(productRequest.getId(), productOrder.getProduct(), productRequest.getRenter(), productRequest.getStatus());
                            receivedRequestsList.add(receivedReq);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("PRODUCT ORDER WITH ID: {} NOT FOUND INSIDE GET RECEIVED REQ", prodId);
                }
            }
        }
        LOGGER.info("GET RECEIVED REQ FOR USER ID: {} LENGTH: {}", sellerId, receivedRequestsList.size());
        return new ResponseEntity<>(receivedRequestsList, HttpStatus.OK);
    }

    @PatchMapping("/markProductOrderInactive")
    public ResponseEntity<ProductOrder> markProductOrderInactive(@RequestParam("productId") String id) {
        try {
            ProductOrder productOrder = getProductOrderById(id).getBody();
            productOrder.setStatus(ProductOrderStatus.INACTIVE);
            cacheService.updateCachedProductOrder(productOrder);
            LOGGER.info("MARKED PRODUCT ORDER INACTIVE ID: {}", id);
            return new ResponseEntity<>(productOrderRepository.save(productOrder), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.warn("PRODUCT ORDER WITH ID: {} NOT FOUND", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //
    @PatchMapping("/acceptProductOrder")
    public ResponseEntity markProductOrderAccepted(@RequestParam("productId") String productId, @RequestParam("requestId") String requestId) {
        Optional<ProductOrder> productOrderOptional = productOrderRepository.findByProductIdAndStatus(productId, ProductOrderStatus.ACTIVE);
        if (productOrderOptional.isPresent()) {
            ProductOrder productOrder = productOrderOptional.get();
            List<ProductRequest> receivedRequestList = productOrder.getReceivedRequests();
            boolean isValidReq = false;
            for (ProductRequest req : receivedRequestList) {
                if (req.getId().equals(requestId) && req.getStatus().equals(RequestStatus.PENDING)) {
                    req.setStatus(RequestStatus.ACCEPTED);
                    isValidReq = true;
                } else {
                    req.setStatus(RequestStatus.DENIED);
                }
            }
            if (isValidReq) {
                productOrder.setReceivedRequests(receivedRequestList);
                productOrder.setStatus(ProductOrderStatus.INACTIVE);
                cacheService.updateCachedProductOrder(productOrder);
                productOrderRepository.save(productOrder);

                String productServiceURL = productServiceBaseURL + "markProductInactive";
                String productUri = UriComponentsBuilder.fromHttpUrl(productServiceURL).queryParam("id", productId).toUriString();
                Product product = restTemplate.patchForObject(productUri, Void.class, Product.class);

                for (ProductRequest productRequest : productOrder.getReceivedRequests()) {
                    sendNotificationEventTo(productRequest.getRenter().getId());
                }
                sendNotificationEventTo(productOrder.getSeller().getId());
                LOGGER.info("ACCEPTED PRODUCT ORDER ID: {} REQ ID: {}", productId, requestId);
                return new ResponseEntity(HttpStatus.OK);
            }
            LOGGER.warn("PRODUCT REQUEST DOESN'T EXIST FOR REQ ID: {} IN PRODUCT ID: {} OR IS NOT IN PENDING STATE", requestId, productId);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        LOGGER.warn("PRODUCT ORDER DOES NOT EXIST");
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PatchMapping("/denyProductOrder")
    public ResponseEntity markProductOrderDenied(@RequestParam("productId") String productId, @RequestParam("requestId") String requestId) {
        Optional<ProductOrder> productOrderOptional = productOrderRepository.findByProductIdAndStatus(productId, ProductOrderStatus.ACTIVE);
        if (productOrderOptional.isPresent()) {
            ProductOrder productOrder = productOrderOptional.get();
            List<ProductRequest> receivedRequestList = productOrder.getReceivedRequests();
            ProductRequest productRequest = null;
            for (ProductRequest req : receivedRequestList) {
                if (req.getId().equals(requestId) && req.getStatus().equals(RequestStatus.PENDING)) {
                    req.setStatus(RequestStatus.DENIED);
                    productRequest = req;
                    break;
                }
            }
            if (productRequest != null) {
                productOrder.setReceivedRequests(receivedRequestList);
                cacheService.updateCachedProductOrder(productOrder);
                productOrderRepository.save(productOrder);

                sendNotificationEventTo(productRequest.getRenter().getId());
                sendNotificationEventTo(productOrder.getSeller().getId());
                LOGGER.info("DENIED PRODUCT ORDER ID: {} REQ ID: {}", productId, requestId);
                return new ResponseEntity(HttpStatus.OK);
            }
            LOGGER.info("PRODUCT REQ DOES NOT EXIST");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        LOGGER.warn("PRODUCT ORDER DOES NOT EXIST");
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getProductIdsForPendingRequestsByUser")
    public ResponseEntity<List<String>> getProductIdsForPendingRequestsByUser(@RequestParam("userId") String userId) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(userId);
        List<String> ret = new ArrayList<>();
        if (userOrderOptional.isPresent()) {
            List<String> sentOrderProdIds = userOrderOptional.get().getSentReqProductIds();
            for (String prodId : sentOrderProdIds) {
                Optional<ProductOrder> productOrderOptional = productOrderRepository.findByProductIdAndStatus(prodId, ProductOrderStatus.ACTIVE);
                if (productOrderOptional.isPresent()) {
                    for (ProductRequest req : productOrderOptional.get().getReceivedRequests()) {
                        if (req.getRenter().getId().equals(userId) && req.getStatus().equals(RequestStatus.PENDING))
                            ret.add(productOrderOptional.get().getProductId());
                    }
                }
            }
        }
        LOGGER.info("getProductIdsForPendingRequestsByUser() RESPONSE LEN: {}", ret.size());
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProductOrder")
    public ResponseEntity deleteProductOrder(@RequestParam("productId") String productId) {
        if (productOrderRepository.existsById(productId)) {
            productOrderRepository.deleteById(productId);
            LOGGER.info("DELETED PRODUCT ORDER ID: {}", productId);
            return new ResponseEntity(HttpStatus.OK);
        }
        LOGGER.warn("PRODUCT ORDER NOT FOUND");
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}
