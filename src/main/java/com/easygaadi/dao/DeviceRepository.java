package com.easygaadi.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This repository provides CRUD operations for {@link Todo}
 * objects.
 * @author Petri Kainulainen
 */
@Repository
public interface DeviceRepository extends CrudRepository<Device, String> {
    Device findByImei(String imei);

}
