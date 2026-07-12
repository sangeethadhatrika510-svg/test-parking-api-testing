package com.example.api.payload;

import com.example.api.model.Location;

public class LocationPayload {
    public static Location locationPayload(int zoneId,String name,
    String address,
    String type,
    boolean active) {
        Location loc = new Location();
        loc.setZoneId(zoneId);
        loc.setName(name);
        loc.setAddress(address);
        loc.setType(type);
        loc.setActive(active);
        return loc;
    }
}
