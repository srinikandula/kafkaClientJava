package com.easygaadi.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.collections4.IteratorUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class DeviceServiceImpl implements DeviceService{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceImpl.class);
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private final DeviceRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    DeviceServiceImpl(DeviceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Device create(Device device) {
        return repository.save(device);
    }

    @Override
    public Device delete(String id) {
        Optional<Device> optionalDevice = repository.findById(id);
        if(optionalDevice.isPresent()){
            repository.delete(optionalDevice.get());
        }
        return optionalDevice.get();
    }

    @Override
    public List<Device> findAll() {
        return IteratorUtils.toList(repository.findAll().iterator());
    }

    @Override
    public Device findById(String id) {
        Optional<Device> optionalDevice = repository.findById(id);
        return optionalDevice.get();
    }

    @Override
    public Device update(Device todo) {
        return null;
    }

    @Override
    public Device findByImei(String imei) {
        return repository.findByImei(imei);
    }

    @Override
    public boolean updateLatestLocation(String deviceId, DevicePosition latestLocation) {
        /**
         * { "latestLocation" : { "location" : { "type" : "Point",
         * "coordinates" : [ 80.081215, 14.272182777777777 ] },
         * "distance" : 0, "totalDistance" : 1804.5515682769392,
         * "_id" : ObjectId("5aeb0e0701ff9a38d4a092c3"),
         * "gprmc" : "$GPRMC,130823.000,A,1416.3310,N,08004.8729,E,0.00,165.00,030518,,*0E",
         * "name" : "{name}", "uniqueId" : "866968030156328",
         * "deviceId" : "866968030156328",
         * "protocol" : "gt06", "deviceTime" : 1525353911125,
         * "fixTime" : 1525352903000, "valid" : false,
         * "altitude" : "0.0", "speed" : "0", "course" : "165.0",
         * "statusCode" : "0xF841",
         * "attributes" : { "alarm" : true, "ignition" : true, "power" : 4, "gsm" : 4, "index" : 269, "ip" : "223.237.1.207" },
         * "address" : "{address}", "isIdle" : false, "isStopped" : false,
         * "createdAt" : ISODate("2018-05-03T13:26:31.912Z"), "updatedAt" : ISODate("2018-05-03T13:26:31.912Z") } }
         */
        /*//BasicDBObject attributes = new BasicDBObject();
        latestLocation.put("gprmc",devicePosition.get("gprmc").toString());
        latestLocation.put("name",devicePosition.getName());
        latestLocation.put("deviceId",devicePosition.getDeviceId());
        latestLocation.put("uniqueId",devicePosition.getUniqueId());
        latestLocation.put("protocol",devicePosition.getProtocol());
        latestLocation.put("deviceTime",devicePosition.getDeviceTime());
        latestLocation.put("fixTime",devicePosition.getFixTime());
        latestLocation.put("altitude",devicePosition.getAltitude());
        latestLocation.put("speed",devicePosition.getSpeed());
        latestLocation.put("course",devicePosition.getCourse());
        latestLocation.put("statusCode",devicePosition.getStatusCode());
        latestLocation.put("distance",devicePosition.getDistance());
        latestLocation.put("totalDistance",devicePosition.getTotalDistance());
        latestLocation.put("address",devicePosition.getAddress());
        latestLocation.put("location",devicePosition.getLocation());*/
        Update update = new Update();
        update.set("attrs.latestLocation", latestLocation);

        final Query query = new Query();
        query.addCriteria(where("imei").is(deviceId));
        UpdateResult updateResult =  mongoTemplate.updateMulti(query, update, Device.class);
        final Query truckQuery = new Query();
        truckQuery.addCriteria(where("deviceId").is(deviceId));
        UpdateResult truckUpdateResult =  mongoTemplate.updateMulti(truckQuery, update, Truck.class);
        if(updateResult.getModifiedCount() != 1) {
            LOGGER.error("Error updating device with imei: {}", deviceId);
        }
        if(truckUpdateResult.getModifiedCount() != 1) {
            LOGGER.error("Error updating truck with deviceId: {}", deviceId);
        }
        return updateResult.getModifiedCount() == 1 && truckUpdateResult.getModifiedCount() ==1;
    }

    @Override
    public boolean updateLatestStatus(String deviceId, DevicePosition latestLocation) {
        Update update = new Update();
        update.set("attrs.latestLocation.isStopped", latestLocation.isStopped());
        update.set("attrs.latestLocation.isIdle", latestLocation.isIdle());
        update.set("attrs.latestLocation.updatedAt", new DateTime());
        // update.set("attrs.latestLocation.deviceTime", latestLocation.getDeviceTime());
        final Query query = new Query();
        query.addCriteria(where("imei").is(deviceId));
        UpdateResult updateResult =  mongoTemplate.updateMulti(query, update, Device.class);
        final Query truckQuery = new Query();
        truckQuery.addCriteria(where("deviceId").is(deviceId));
        UpdateResult truckUpdateResult =  mongoTemplate.updateMulti(truckQuery, update, Truck.class);
        if(updateResult.getModifiedCount() != 1) {
            LOGGER.error("Error updating device with imei: {}", deviceId);
        }
        if(truckUpdateResult.getModifiedCount() != 1) {
            LOGGER.error("Error updating truck with deviceId: {}", deviceId);
        }
        return updateResult.getModifiedCount() == 1 && truckUpdateResult.getModifiedCount() ==1;
    }
}
