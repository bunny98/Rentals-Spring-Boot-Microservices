package com.rentals.collegeservice.controller;

import com.rentals.collegeservice.model.College;
import com.rentals.collegeservice.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    @Cacheable(value = "colleges", key = "#id", unless = "#result.numOfStudents < 5")
    public College getCollegeById(@RequestParam("id") String id) {

        System.out.println("FETCHING COLLEGE WITH ID "+id);
        Optional<College> collegeData = collegeRepository.findById(id);
        if (collegeData.isPresent()) {
            return  collegeData.get();
        }
        return null;
    }

    @PatchMapping("/increaseStudentCount")
    @CachePut(value = "colleges", key = "#college.id")
    public ResponseEntity increaseStudentCount(@RequestParam ("id") String id){
        Optional<College> collegeData = collegeRepository.findById(id);
        if (collegeData.isPresent()) {
            College college = collegeData.get();
            college.setNumOfStudents(college.getNumOfStudents() + 1);
            collegeRepository.save(college);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<College> createCollege(@Valid College college) {
        try {
            College newCollege = collegeRepository.save(new College(college.getName(), 0));
            return new ResponseEntity<>(newCollege, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    @CacheEvict(value = "colleges", key = "#user.id")
    public ResponseEntity delete(@RequestParam("id") String id) {
        try {
            collegeRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteAll")
    @CacheEvict(value = "colleges", allEntries = true)
    public  ResponseEntity deleteAll(){
        try{
            collegeRepository.deleteAll();
            return new ResponseEntity(null,HttpStatus.OK);
        }catch (Exception e){
            return  new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
