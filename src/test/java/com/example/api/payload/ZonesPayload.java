package com.example.api.payload;

import com.example.api.model.Zones;

public class ZonesPayload {
    public static Zones zonePayload(String code,
    String name,
    String city,
    String description,
    boolean active){
        Zones zs=new Zones();
        zs.setCode(code);
        zs.setName(name);
        zs.setCity(city);
        zs.setDescription(description);
        zs.setActive(active);
        return zs;

    }

}
