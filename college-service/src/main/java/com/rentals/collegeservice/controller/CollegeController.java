package com.rentals.collegeservice.controller;

import com.rentals.collegeservice.model.College;
import com.rentals.collegeservice.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/college")
public class CollegeController {
    @Autowired
    private CollegeRepository collegeRepository;

    @GetMapping("/getAll")
    public ResponseEntity<List<College>> getAll() {
        List<College> colleges = collegeRepository.findAll();
        return new ResponseEntity<>(colleges, HttpStatus.OK);
    }

    @GetMapping("/getById")
    public ResponseEntity<College> getCollegeById(@RequestParam("id") String id) {
        Optional<College> collegeData = collegeRepository.findById(id);
        if (collegeData.isPresent()) {
            return new ResponseEntity<>(collegeData.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }


    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<College> createCollege(College college) {
        try {
            College newCollege = collegeRepository.save(new College(college.getName()));
            return new ResponseEntity<>(newCollege, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteHostel(@RequestParam("id") String id) {
        try {
            collegeRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
