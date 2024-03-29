package com.rentals.userservice.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;

public class User {
    @Id
    private String id;

    @NotNull
    private String name;
    @NotNull
    private String collegeId;
    @NotNull
    private String mobileNumber;

    public User() {

    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", collegeId='" + collegeId + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
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
