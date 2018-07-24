package com.easygaadi.dao;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TruckRepository extends CrudRepository<Truck, String> {
    List<Truck> findByAccountId(ObjectId accountId);
}
