package com.rentals.ordersservice.model;

import org.springframework.data.annotation.Id;

public class User {
    @Id
    private String id;

    private String name;
    private String collegeId;
    private String mobileNumber;

    public User() {

    }

    public User(String name, String collegeId, String mobileNumber) {
        this.name = name;
        this.collegeId = collegeId;
        this.mobileNumber = mobileNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(String collegeId) {
        this.collegeId = collegeId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
