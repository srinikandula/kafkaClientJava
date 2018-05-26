package com.easygaadi.kafka.consumer;


import com.easygaadi.dao.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public final class Receiver {
    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DevicePositionRepository devicePositionRepository;

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
            DevicePosition devicePositions = objectMapper.readValue(message, DevicePosition.class);
            LOG.info("address :{} ", devicePositions.getAddress());
            process(devicePositions);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void process(DevicePosition currentLocation) throws Exception{
        Device device = deviceService.findByImei(currentLocation.getUniqueId());
        if(device == null){
            //unknownPositionsDAO.save(currentPosition);
            LOG.error("unknown position: {}", currentLocation);
        } else {

            if(device.getAttrs().get("latestLocation") == null){
                LOG.warn("no last location was found");
                currentLocation.setDistance(0);
                currentLocation.setTotalDistance(0);
                Location location = new Location();
                List<Double> coordinates = new ArrayList<>();
                coordinates.add(currentLocation.getLongitude());
                coordinates.add(currentLocation.getLatitude());
                location.setCoordinates(coordinates);
                currentLocation.setLocation(location);

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
                LOG.info("Loading last location");
                DevicePosition lastLocation = objectMapper.readValue(
                        device.getAttrs().get("latestLocation").toString(), DevicePosition.class);

                List<Double> lastCoordinates = lastLocation.getLocation().getCoordinates();

                if (lastCoordinates.get(0) == currentLocation.getLongitude() &&
                        lastCoordinates.get(1) == currentLocation.getLatitude()) {
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
                } else { //calculate the distance travelled
                    currentLocation.setIdle(false);
                    currentLocation.setStopped(false);

                    double lastLatitude = lastCoordinates.get(1);
                    double lastLongitude = lastCoordinates.get(0);
                    double currentLatitude = currentLocation.getLatitude();
                    double currentLongitude = currentLocation.getLongitude();
                    //position.distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((latitude-position.location.coordinates[1])*Math.PI/180 /2),2)+Math.cos(latitude*Math.PI/180)*Math.cos(position.location.coordinates[1]*Math.PI/180)*Math.pow(Math.sin((longitude-position.location.coordinates[0])*Math.PI/180/2),2)))
                    currentLocation.setDistance(1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((currentLatitude - lastLatitude) * Math.PI / 180 / 2), 2) + Math.cos(lastLatitude * Math.PI / 180) * Math.cos(currentLatitude * Math.PI / 180) * Math.pow(Math.sin((currentLongitude - lastLongitude) * Math.PI / 180 / 2), 2))));
                    currentLocation.setTotalDistance(lastLocation.getTotalDistance() + currentLocation.getDistance());
                    devicePositionRepository.save(currentLocation);
                    if(!deviceService.updateLatestLocation(device.getImei(), currentLocation)){
                        LOG.error("FAIL....");
                    } else {
                        LOG.info("Done...");
                    }
                    LOG.info("processed deviceId:{}, totalDistance :{}, distance;{}", currentLocation.getUniqueId(), currentLocation.getTotalDistance(), currentLocation.getDistance());
                }
            }
        }
    }



}
