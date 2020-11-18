package com.rentals.userservice.controller;

import com.rentals.userservice.model.User;
import com.rentals.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<User> createUser(User user){
        try{
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
}
