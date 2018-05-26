package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private String type ="Point";
    private double[] coordinates;
}
