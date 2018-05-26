package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Location {
    private String type ="Point";
    private List<Double> coordinates;
}
