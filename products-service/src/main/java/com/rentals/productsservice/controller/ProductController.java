package com.rentals.productsservice.controller;

import com.rentals.productsservice.model.Product;
import com.rentals.productsservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Product> createProduct(Product product) {
        try {
            Product newProduct = productRepository.save(new Product(product.getName(), product.getSellerId(), product.getCollegeId(), "ACTIVE", product.getPrice(), product.getContentURLs()));
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = new ArrayList<>(productRepository.findAll());
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<Product> getProductWithId(@RequestParam("id") String id) {
        Optional<Product> productData = productRepository.findById(id);
        return productData.map(product -> new ResponseEntity<>(product, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/getByCollegeId")
    public ResponseEntity<List<Product>> getProductWithCollegeId(@RequestParam("collegeId") String collegeId){
        List<Product> products = productRepository.findByCollegeIdAndStatus(collegeId, "ACTIVE");
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/getMyProducts")
    public ResponseEntity<List<Product>> getMyProducts(@RequestParam("sellerId") String sellerId){
        List<Product> products = productRepository.findBySellerIdAndStatus(sellerId, "ACTIVE");
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PutMapping(value="/updateProduct", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Product> updateProduct(@RequestParam("id") String id, Product product){
        Optional<Product> productData = productRepository.findById(id);
        if(productData.isPresent()){
            Product prod = productData.get();
            prod.setName(product.getName());
            prod.setSellerId(product.getSellerId());
            prod.setCollegeId(product.getCollegeId());
            prod.setPrice(product.getPrice());
            prod.setContentURLs(product.getContentURLs());
            prod.setStatus(product.getStatus());
            return new ResponseEntity<>(productRepository.save(prod), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/markProductInactive")
    public ResponseEntity<Product> markProductInactive(@RequestParam("id") String id){
        Optional<Product> productData = productRepository.findById(id);
        if(productData.isPresent()){
            Product product = productData.get();
            product.setStatus("INACTIVE");
            return new ResponseEntity<>(productRepository.save(product), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteProductWithId(@RequestParam("id") String id){
        try {
            productRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
