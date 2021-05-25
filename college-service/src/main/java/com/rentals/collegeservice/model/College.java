package com.rentals.collegeservice.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;


public class College implements Serializable {
    @Id
    private String id;

    @NotNull
    private String name;

    private int numOfStudents;

    public College() { }

    public College(String name, int numOfStudents) {
        this.name = name;
        this.numOfStudents = numOfStudents;
    }

    public void setNumOfStudents(int numOfStudents) {
        this.numOfStudents = numOfStudents;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumOfStudents() {
        return numOfStudents;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "College{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", numOfStudents=" + numOfStudents +
                '}';
    }

}
