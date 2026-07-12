package com.example.api.payload;

import com.example.api.model.Location;
import com.example.api.model.LocationSpace;

public class LocationSpacePayload {
    public static LocationSpace locationSpacePayload(String code,
                                           String status,
                                           boolean reservable) {
        LocationSpace locspace=new LocationSpace();
        locspace.setCode(code);
        locspace.setStatus(status);
        locspace.setReservable(reservable);
        return locspace;
    }
}
