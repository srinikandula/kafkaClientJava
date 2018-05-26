package com.easygaadi.dao;

import org.springframework.data.repository.CrudRepository;

/**
 * This repository provides CRUD operations for {@link Todo}
 * objects.
 * @author Petri Kainulainen
 */
public interface DeviceRepository extends CrudRepository<Device, String> {
    Device findByImei(String imei);

}
