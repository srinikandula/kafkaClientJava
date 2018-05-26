package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attributes {
    /*
    "attributes" : { "sat" : 15, "ignition" : false, "distance" : 47.27, "totalDistance" : 2686263.83, "motion" : true }
    "attributes":["{\"status\":70,\"ignition\":true,\"charge\":true,\"blocked\":false,\"battery\":6,\"rssi\":4,\"distance\":0.0,\"totalDistance\":7.772386046E7,\"motion\":true}"],

     */
    private int sat;
    private boolean ignition;
    private double distance;
    private double totalDistance;
    private boolean motion;
    private boolean charge;
    private boolean blocked;
    private int battery;
    private int rssi;
}
