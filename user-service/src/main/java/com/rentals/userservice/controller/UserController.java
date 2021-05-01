package com.rentals.userservice.controller;

import com.rentals.userservice.model.User;
import com.rentals.userservice.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;

    private String collegeServiceURL = "http://colleges-service/college/";

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<User> createUser(@Valid User user){
        try{
            String uri = UriComponentsBuilder.fromHttpUrl(collegeServiceURL + "increaseStudentCount").queryParam("id", user.getCollegeId()).toUriString();
            ResponseEntity response = restTemplate.exchange(uri, HttpMethod.PATCH, HttpEntity.EMPTY, ResponseEntity.class);
            if(response.getStatusCode() == HttpStatus.NOT_FOUND)
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            User newUser = userRepository.save(new User(user.getName(), user.getCollegeId(), user.getMobileNumber()));
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<User> getById(@RequestParam("id") String id){
        Optional<User> userData = userRepository.findById(id);
        return userData.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/checkUserAndCollege")
    public ResponseEntity checkUserAndCollege(@RequestParam("userId") String userId, @RequestParam("collegeId") String collegeId){
        System.out.println("CHECK USER AND COLLEGE METHOD");
        Optional<User> userData = userRepository.findById(userId);
        if(userData.isPresent()) {
            String userCollegeId = userData.get().getCollegeId();
            if(userCollegeId.equals(collegeId)) {
                System.out.println("RETURNING SUCCESS");
                return new ResponseEntity( HttpStatus.OK);
            }
            System.out.println("RETURNING FAILURE BAD REQ");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        System.out.println("RETURNING FAILURE NOT FOUND");
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/delete")
    public ResponseEntity deleteUserById(@RequestParam("id") String id){
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity deleteAll(){
        try{
            userRepository.deleteAll();
            return new ResponseEntity(null, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
