package com.easygaadi.kafka.consumer;


import com.easygaadi.dao.Device;

import com.easygaadi.dao.DevicePositions;
import com.easygaadi.dao.GpsSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
final class Receiver {
    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);
    private ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private MongoTemplate mongoTemplate;


    private Map<String,GpsSettings> accountGPSSettings = new HashMap<>();
    public Receiver(){
        objectMapper.registerModule(new JodaModule());
    }

    @KafkaListener(topics = "${app.topic.deviceLocations}")
    public void listen(@Payload String message)  {
        try {
            DevicePositions devicePositions = objectMapper.readValue(message, DevicePositions.class);
            Position position = new Position(devicePositions.getLatitude(), devicePositions.getLongitude());
            devicePositions.setLocation(position);
            LOG.info("address :{} ", devicePositions.getAddress());
            //process(devicePositions);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public void process(DevicePositions currentPosition) throws Exception{
        Device device = deviceService.findByImei(currentPosition.getUniqueId());
        if(device == null){
            //unknownPositionsDAO.save(currentPosition);
            LOG.error("unknown position: {}", currentPosition);
        } else {
            GpsSettings accountSettings = accountGPSSettings.get(device.getAccountId());
            if(accountSettings == null){
                accountSettings = gpsSettingsDAO.findByAccountId(device.getAccountId());
                accountGPSSettings.put(device.getAccountId(), accountSettings);
            }
            if(device.getAttrs().get("lastLocation") == null){
                currentPosition.setDistance(0);
                currentPosition.setTotalDistance(0);
                device.getAttrs().put("latestLocation", objectMapper.writeValueAsString(currentPosition));
                device = deviceDAO.save(device);
            } else {
                DevicePositions lastLocation = objectMapper.readValue(
                        device.getAttrs().get("latestLocation").toString(), DevicePositions.class);
                long idealTime = 20 * 60000;
                if (accountSettings.getMinStopTime() != 0) {
                    idealTime = accountSettings.getMinStopTime() * 60000;
                }
                long stopTime = 30 * 60000;
                if (accountSettings.getMinStopTime() != 0) {
                    stopTime = accountSettings.getMinStopTime() * 60000;
                }

                if (lastLocation.getLocation().getValues().get(0) == currentPosition.getLocation().getValues().get(0) &&
                        lastLocation.getLocation().getValues().get(1) == currentPosition.getLocation().getValues().get(1)) {
                    if (lastLocation.isIdle()) {
                        if (System.currentTimeMillis() - lastLocation.getUpdatedAt().getMillis() > stopTime) {
                            currentPosition.setIdle(true);
                            currentPosition.setStopped(true);
                        }
                    } else {
                        currentPosition.setIdle(false);
                        currentPosition.setStopped(false);
                    }
                } else { //calculate the distance travelled
                    currentPosition.setIdle(false);
                    currentPosition.setStopped(false);

                    double lastLatitude = lastLocation.getLocation().getValues().get(1);
                    double lastLongitude = lastLocation.getLocation().getValues().get(0);
                    double currentLatitude = currentPosition.getLocation().getValues().get(1);
                    double currentLongitude = lastLocation.getLocation().getValues().get(0);
                    //position.distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((latitude-position.location.coordinates[1])*Math.PI/180 /2),2)+Math.cos(latitude*Math.PI/180)*Math.cos(position.location.coordinates[1]*Math.PI/180)*Math.pow(Math.sin((longitude-position.location.coordinates[0])*Math.PI/180/2),2)))
                    currentPosition.setDistance(1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((currentLatitude - lastLatitude) * Math.PI / 180 / 2), 2) + Math.cos(lastLatitude * Math.PI / 180) * Math.cos(currentLatitude * Math.PI / 180) * Math.pow(Math.sin((currentLongitude - lastLongitude) * Math.PI / 180 / 2), 2))));
                    currentPosition.setTotalDistance(lastLocation.getTotalDistance() + currentPosition.getDistance());
                    devicePositionsDAO.save(currentPosition);
                    //mongoTemplate.fin
                    LOG.info("processed deviceId:{}, totalDistance :{}", currentPosition.getUniqueId(), currentPosition.getTotalDistance());
                }
            }
        }
    }
    */


}
