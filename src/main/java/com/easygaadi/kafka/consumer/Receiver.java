package com.easygaadi.kafka.consumer;


import com.easygaadi.dao.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public final class Receiver {
    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private GpsSettingsRepository gpsSettingsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;


    private Map<String,GpsSettings> accountGPSSettings = new HashMap<>();
    public Receiver(){
        objectMapper.registerModule(new JodaModule());
    }

    @KafkaListener(topics = "${app.topic.deviceLocations}")
    public void listen(@Payload String message)  {
        try {
            BasicDBObject devicePositions = objectMapper.readValue(message, BasicDBObject.class);
            LOG.info("address :{} ", devicePositions.get("address"));
            process(devicePositions.get("uniqueId").toString(), devicePositions);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void process(String uniqueId, BasicDBObject currentLocation) throws Exception{
        Device device = deviceService.findByImei(uniqueId);
        if(device == null){
            //unknownPositionsDAO.save(currentPosition);
            LOG.error("unknown position: {}", currentLocation);
        } else {

            if(device.getAttrs().get("latestLocation") == null){
                LOG.warn("no last location was found");
                currentLocation.put("distance", 0);
                currentLocation.put("totalDistance", 0);
                KafkaController.createLocation(currentLocation);

                //device.getAttrs().put("latestLocation", objectMapper.writeValueAsString(currentPosition));
                if(!deviceService.updateLatestLocation(device.getImei(), currentLocation)){
                    LOG.error("FAIL....");
                } else {
                    LOG.info("Done...");
                }
            } else {
                //DevicePosition devicePosition = objectMapper.readValue(device.getAttrs().get("latestLocation"), DevicePosition.class);
                //LOG.error("address : {}", devicePosition.getAddress());
                GpsSettings accountSettings = accountGPSSettings.get(device.getAccountId());
                if(accountSettings == null){
                    accountSettings = gpsSettingsRepository.findByAccountId(new ObjectId(device.getAccountId()));
                    accountGPSSettings.put(device.getAccountId(), accountSettings);
                }
                long idealTime = 20 * 60000;
                if (accountSettings != null && accountSettings.getMinStopTime() != 0) {
                    idealTime = accountSettings.getMinStopTime() * 60000;
                }
                long stopTime = 30 * 60000;
                if (accountSettings != null && accountSettings.getMinStopTime() != 0) {
                    stopTime = accountSettings.getMinStopTime() * 60000;
                }

                Object object = device.getAttrs().get("latestLocation");
                try {
                    Map lastLocationJSON = (LinkedHashMap) object;
                    Map lastLocation = convertToDevicePosition(lastLocationJSON);
                    if (lastLocation.get("location") != null) {
                        List<Double> lastCoordinates = (List<Double>)((Map)lastLocation.get("location")).get("coordinates");
                        if (lastCoordinates.get(0) == Double.parseDouble(currentLocation.get("longitude").toString()) &&
                                lastCoordinates.get(1) == Double.parseDouble(currentLocation.get("latitude").toString())) {
                            LOG.info("Same as old location");
                            /*if (lastLocation.isIdle()) {
                                if (System.currentTimeMillis() - lastLocation.getUpdatedAt().getMillis() > stopTime) {
                                    currentPosition.setIdle(true);
                                    currentPosition.setStopped(true);
                                }
                            } else {
                                currentPosition.setIdle(false);
                                currentPosition.setStopped(false);
                            }*/
                            return;
                        } else { //calculate the distance travelled
                            currentLocation.put("isIdle", false);
                            currentLocation.put("isStopped", false);
                            double lastLongitude = lastCoordinates.get(0);
                            double lastLatitude = lastCoordinates.get(1);
                            double currentLatitude = Double.parseDouble(currentLocation.get("latitude").toString());
                            double currentLongitude = Double.parseDouble(currentLocation.get("longitude").toString());
                            //position.distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((latitude-position.location.coordinates[1])*Math.PI/180 /2),2)+Math.cos(latitude*Math.PI/180)*Math.cos(position.location.coordinates[1]*Math.PI/180)*Math.pow(Math.sin((longitude-position.location.coordinates[0])*Math.PI/180/2),2)))
                            double distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((currentLatitude - lastLatitude) * Math.PI / 180 / 2), 2) + Math.cos(lastLatitude * Math.PI / 180) * Math.cos(currentLatitude * Math.PI / 180) * Math.pow(Math.sin((currentLongitude - lastLongitude) * Math.PI / 180 / 2), 2)));
                            currentLocation.put("distance", distance);
                            currentLocation.put("distance", distance);
                            currentLocation.put("totalDistance", Double.parseDouble(lastLocation.get("totalDistance").toString()) + distance);;
                            mongoTemplate.save(currentLocation, "devicePositions");
                        }
                    } else {
                        LOG.info("no location was found in the last location");
                        KafkaController.createLocation(currentLocation);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(!deviceService.updateLatestLocation(device.getImei(), currentLocation)){
                    LOG.error("FAIL....");
                } else {
                    LOG.info("Done...");
                }
                LOG.info("processed deviceId:{}, totalDistance :{}, distance;{}", currentLocation.get("uniqueId"), currentLocation.get("totalDistance"), currentLocation.get("distance"));
            }
        }
    }

    private Map<String, Object> convertToDevicePosition(Map lastLocationJSON) {
        Map<String, Object> devicePosition = new HashMap<>();
        if(lastLocationJSON.containsKey("totalDistance")){
            devicePosition.put("totalDistance", (Double.parseDouble(lastLocationJSON.get("totalDistance").toString())));
        } else {
            devicePosition.put("totalDistance", 0);
        }
        if(lastLocationJSON.containsKey("location")){
            Map<String, Object>  loc = (Map<String, Object>)lastLocationJSON.get("location");
            devicePosition.put("location", loc);
        }
        return devicePosition;
    }
}
