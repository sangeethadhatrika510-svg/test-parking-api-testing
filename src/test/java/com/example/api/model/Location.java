package com.example.api.model;

import lombok.Getter;
import lombok.Setter;


    @Setter
    @Getter
    public class Location {

        int zoneId;
        String name;
        String address;
        String type;
        boolean active;
    }

