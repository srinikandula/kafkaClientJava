package com.easygaadi.dao;

import org.bson.types.ObjectId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeoFenceReportRepository extends CrudRepository<GeoFenceReport, String> {
    List<GeoFenceReport> findByAccountId(ObjectId accountId);
}
