package com.rentals.collegeservice.controller;

import com.rentals.collegeservice.model.College;
import com.rentals.collegeservice.repository.CollegeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger LOGGER = LoggerFactory.getLogger(CollegeController.class);

    @GetMapping("/getAll")
    public ResponseEntity<List<College>> getAll() {
        List<College> colleges = collegeRepository.findAll();
        LOGGER.info("getAll colleges length: {}", colleges.size());
        return new ResponseEntity<>(colleges, HttpStatus.OK);
    }

    @GetMapping("/getById")
    @Cacheable(value = "colleges", key = "#id", unless = "#result.numOfStudents < 5")
    public College getCollegeById(@RequestParam("id") String id) {

        System.out.println("FETCHING COLLEGE WITH ID "+id);
        Optional<College> collegeData = collegeRepository.findById(id);
        if (collegeData.isPresent()) {
            LOGGER.info("getCollegeById RESPONSE: {}", collegeData.get());
            return  collegeData.get();
        }
        LOGGER.warn("getCollegeById RESPONSE: NOT FOUND");
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
            LOGGER.info("INCREASED STUDENT COUNT IN COLLEGE {}", id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        LOGGER.warn("increaseStudentCount() COLLEGE NOT FOUND ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<College> createCollege(@Valid College college) {
        try {
            College newCollege = collegeRepository.save(new College(college.getName(), 0));
            LOGGER.info("CREATED COLLEGE : {}", college.toString());
            return new ResponseEntity<>(newCollege, HttpStatus.CREATED);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION CREATING COLLEGE {}\n{}", college.toString(),e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    @CacheEvict(value = "colleges", key = "#user.id")
    public ResponseEntity delete(@RequestParam("id") String id) {
        try {
            collegeRepository.deleteById(id);
            LOGGER.info("COLLEGE DELETED ID: {}", id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("EXCEPTION DELETING COLLEGE ID: {}\n{}", id, e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteAll")
    @CacheEvict(value = "colleges", allEntries = true)
    public  ResponseEntity deleteAll(){
        try{
            collegeRepository.deleteAll();
            LOGGER.info("DELETED ALL COLLEGES");
            return new ResponseEntity(null,HttpStatus.OK);
        }catch (Exception e){
            LOGGER.error("EXCEPTION DELETING ALL COLLEGES {}", e.getMessage());
            return  new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
