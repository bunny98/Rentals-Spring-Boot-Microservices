package com.rentals.productsservice.controller;

import com.rentals.productsservice.model.Product;
import com.rentals.productsservice.model.UserProduct;
import com.rentals.productsservice.model.UserProductStatus;
import com.rentals.productsservice.repository.ProductRepository;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RestTemplate restTemplate;

    private final String orderServiceBaseURL = "http://orders-service/order/";
    private final String userServiceBaseURL = "http://users-service/user/";

    private final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Product> createProduct(@Valid Product product) {
        try {
            String userServiceURL = userServiceBaseURL + "checkUserAndCollege";
            String uri = UriComponentsBuilder.fromHttpUrl(userServiceURL).queryParam("userId", product.getSellerId()).queryParam("collegeId", product.getCollegeId()).toUriString();
            try {
                restTemplate.getForEntity(uri, Void.class);
            }catch (Exception e){
                LOGGER.info("USER {} DOESN'T BELONG TO THE COLLEGE {}", product.getSellerId(), product.getCollegeId());
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            Product newProduct = productRepository.save(new Product(product.getName(), product.getSellerId(), product.getCollegeId(), "ACTIVE", product.getPrice(), product.getContentURLs()));
            LOGGER.info("CREATED NEW PRODUCT {}", newProduct.toString());
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.error("ERROR CREATING NEW PRODUCT {}\n{}", product.toString(), e.getMessage());
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = new ArrayList<>(productRepository.findAll());
            LOGGER.info("GET ALL PRODUCTS LEN: {}", products.size());
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("ERROR GETTING ALL PRODUCTS \n{}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<Product> getProductWithId(@RequestParam("id") String id) {
        Optional<Product> productData = productRepository.findById(id);
        if(productData.isPresent()){
            Product product = productData.get();
            LOGGER.info("GET PRODUCT BY ID: {} PRODUCT: {}", id, product.toString());
            return new ResponseEntity<>(product, HttpStatus.OK);
        }
        LOGGER.warn("PRODUCT WITH ID {} NOT FOUND", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getByCollegeId")
    public ResponseEntity<List<Product>> getProductsWithCollegeId(@RequestParam("collegeId") String collegeId){
        List<Product> products = productRepository.findByCollegeIdAndStatus(collegeId, "ACTIVE");
        LOGGER.info("GET PRODUCTS BY COLLEGE ID {} LEN: {}", collegeId,products.size());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/getLobbyProductsByUser")
    public ResponseEntity<List<UserProduct>> getLobbyProducts(@RequestParam("userId") String userId, @RequestParam("collegeId") String collegeId){
        List<Product> myCollegeProducts = getProductsWithCollegeId(collegeId).getBody();
        Set<String> myProductsSet = getMyProducts(userId).getBody().stream().map((product -> product.getId())).collect(Collectors.toSet());
        String ordersServiceUrl = orderServiceBaseURL + "getProductIdsForPendingRequestsByUser";
        String uri = UriComponentsBuilder.fromHttpUrl(ordersServiceUrl).queryParam("userId", userId).toUriString();
        List requestedProductIds = restTemplate.getForObject(uri, List.class);
        Set<String> requestedProductsSet = new HashSet<String>(requestedProductIds);

        List<UserProduct> userProducts = new ArrayList<>();
        for(Product prod: myCollegeProducts){
            UserProduct userProduct = new UserProduct(prod.getId(), prod.getName(), prod.getPrice(), prod.getContentURLs(), prod.getSellerId(), UserProductStatus.AVAILABLE);
            if(myProductsSet.contains(prod.getId())){
                userProduct.setStatus(UserProductStatus.MYPRODUCT);
            }
            if(requestedProductsSet.contains(prod.getId())){
                userProduct.setStatus(UserProductStatus.REQUESTED);
            }
            userProducts.add(userProduct);
        }
        LOGGER.info("GET LOBBY PRODUCTS BY USER {} COLLEGE ID: {} LEN: {}", userId, collegeId, userProducts.size());
        return new ResponseEntity<>(userProducts, HttpStatus.OK);
    }

    @GetMapping("/getMyProducts")
    public ResponseEntity<List<Product>> getMyProducts(@RequestParam("sellerId") String sellerId){
        List<Product> products = productRepository.findBySellerIdAndStatus(sellerId, "ACTIVE");
        LOGGER.info("GET MY PRODUCTS USER ID: {} LEN: {}", sellerId, products.size());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/getStatus")
    public ResponseEntity<String> getProductStatus(@RequestParam("id") String id){
        Optional<Product> productData = productRepository.findById(id);
        if(productData.isPresent()){
            Product product = productData.get();
            LOGGER.info("GET PRODUCT STATUS ID: {} STATUS: {}", id, product.getStatus());
            return new ResponseEntity<>(product.getStatus(), HttpStatus.OK);
        }
        LOGGER.warn("GET PRODUCT STATUS ID: {} NOT FOUND", id);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PatchMapping(value="/update", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Product> updateProduct(@RequestParam("id") String id, Product product){
        Optional<Product> productData = productRepository.findById(id);
        if(productData.isPresent()){
            Product prod = productData.get();
            prod.setName(product.getName());
            prod.setPrice(product.getPrice());
            prod.setContentURLs(product.getContentURLs());
            Product newProduct = productRepository.save(prod);
            LOGGER.info("UPDATED PRODUCT: {}", newProduct.toString());
            return new ResponseEntity<>(newProduct, HttpStatus.OK);
        }
        LOGGER.warn("UPDATE PRODUCT NOT FOUND ID: {}", id);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PatchMapping(value="/markProductInactive")
    public ResponseEntity<Product> markProductInactive(@RequestParam("id") String id){
        Optional<Product> productData = productRepository.findById(id);
        if(productData.isPresent()){
            Product product = productData.get();
            product.setStatus("INACTIVE");
            Product newProd = productRepository.save(product);
            LOGGER.info("MARKED PRODUCT INACTIVE ID {}", id);
            return new ResponseEntity<>(newProd, HttpStatus.OK);
        }
        LOGGER.warn("MARK PRODUCT INACTIVE PRODUCT NOT FOUND ID {}", id);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteProductWithId(@RequestParam("id") String id){
        if(productRepository.existsById(id)){
            productRepository.deleteById(id);
            String orderServiceURL = orderServiceBaseURL + "denyOrdersByProduct";
            String uri = UriComponentsBuilder.fromHttpUrl(orderServiceURL).queryParam("id", id).toUriString();
            restTemplate.delete(uri);
            LOGGER.info("DELETED PRODUCT ID: {}", id);
            return new ResponseEntity(HttpStatus.OK);
        }
        LOGGER.warn("DELETE PRODUCT ID {} NOT FOUND", id);
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}
