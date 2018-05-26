package com.easygaadi.dao;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;

/**
 * This repository provides CRUD operations for {@link Todo}
 * objects.
 * @author Petri Kainulainen
 */
public interface GpsSettingsRepository extends CrudRepository<GpsSettings, String> {
    GpsSettings findByAccountId(ObjectId accountId);

}
