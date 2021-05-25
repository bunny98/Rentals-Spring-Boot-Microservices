package com.rentals.userservice.controller;

import com.rentals.userservice.model.User;
import com.rentals.userservice.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;

    private String collegeServiceURL = "http://colleges-service/college/";

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<User> createUser(@Valid User user){
        try{
            String uri = UriComponentsBuilder.fromHttpUrl(collegeServiceURL + "increaseStudentCount").queryParam("id", user.getCollegeId()).toUriString();
            ResponseEntity response = restTemplate.exchange(uri, HttpMethod.PATCH, HttpEntity.EMPTY, ResponseEntity.class);
            if(response.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.warn("CREATE USER COLLEGE ID WRONG");
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            User newUser = userRepository.save(new User(user.getName(), user.getCollegeId(), user.getMobileNumber()));
            LOGGER.info("CREATED USER : {}", newUser.toString());
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        }catch(Exception e){
            LOGGER.error("EXCEPTION CREATING USER : {}\n{}", user.toString(), e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<User> getById(@RequestParam("id") String id){
        Optional<User> userData = userRepository.findById(id);
        if(userData.isPresent()){
            User user = userData.get();
            LOGGER.info("GET USER BY ID: {}", id);
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        LOGGER.warn("GET USER BY ID {} NOT FOUND", id);
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userRepository.findAll();
        LOGGER.info("GET ALL USERS LEN {}", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/checkUserAndCollege")
    public ResponseEntity checkUserAndCollege(@RequestParam("userId") String userId, @RequestParam("collegeId") String collegeId){
        System.out.println("CHECK USER AND COLLEGE METHOD");
        Optional<User> userData = userRepository.findById(userId);
        if(userData.isPresent()) {
            String userCollegeId = userData.get().getCollegeId();
            if(userCollegeId.equals(collegeId)) {
                LOGGER.info("CHECK USER {} AND COLLEGE {} RETURNING SUCCESS", userId, collegeId);
                return new ResponseEntity( HttpStatus.OK);
            }
            LOGGER.info("CHECK USER {} AND COLLEGE {} RETURNING FAILURE", userId, collegeId);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("CHECK USER {} AND COLLEGE {} RETURNING USER NOT FOUND", userId, collegeId);
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/delete")
    public ResponseEntity deleteUserById(@RequestParam("id") String id){
        if(userRepository.existsById(id)){
            userRepository.deleteById(id);
            LOGGER.info("DELETED USER BY ID {}", id);
            return new ResponseEntity(HttpStatus.OK);
        }
        LOGGER.warn("DELETE USER BY ID {} NOT FOUND", id);
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity deleteAll(){
        try{
            userRepository.deleteAll();
            LOGGER.info("DELETED ALL USERS");
            return new ResponseEntity(null, HttpStatus.OK);
        }catch (Exception e){
            LOGGER.error("EXCEPTION DELETING ALL USERS {}", e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
