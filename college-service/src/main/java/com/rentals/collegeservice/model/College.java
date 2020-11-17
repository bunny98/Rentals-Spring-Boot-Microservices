package com.rentals.collegeservice.model;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.ArrayList;

public class College {
    @Id
    private String id;

    private String name;

    public College() { }

    public College(String name) {
        this.name = name;
    }

    public College(String name, List<String> hostels) {
        this.name = name;
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

}
