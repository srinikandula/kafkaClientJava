package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@Document(collection = "geoFences")
public final class GeoFence extends AbstractDocument {
    private String name;
    private String address;
    private double radius;
    private Map<String, Object> geoLocation;
    private String accountId;

}
