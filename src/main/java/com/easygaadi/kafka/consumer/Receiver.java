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
    private MongoTemplate mongoTemplate;


    @Autowired
    private GpsSettingsRepository gpsSettingsRepository;

    @Autowired
    private DevicePositionRepository devicePositionRepository;

    private Map<String,GpsSettings> accountGPSSettings = new HashMap<>();
    public Receiver(){
        objectMapper.registerModule(new JodaModule());
    }

    @KafkaListener(topics = "${app.topic.deviceLocations}")
    public void listen(@Payload String message)  {
        try {
            DevicePosition devicePositions = objectMapper.readValue(message, DevicePosition.class);
            process(devicePositions.getUniqueId().toString(), devicePositions);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void process(String uniqueId, DevicePosition currentLocation) throws Exception{
        LOG.info("uniqueId :{}", uniqueId);
        Device device = deviceService.findByImei(uniqueId);
        if(device == null){
            //unknownPositionsDAO.save(currentPosition);
            LOG.error("unknown position: {}", currentLocation);
        } else {

            if(device.getAttrs().get("latestLocation") == null){
                LOG.warn("no last location was found");
                currentLocation.setDistance(0);
                currentLocation.setTotalDistance(0);
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
                long idealTime = 10 * 60000;
                if (accountSettings != null && accountSettings.getMinStopTime() != 0) {
                    idealTime = accountSettings.getMinStopTime() * 60000;
                }
                long stopTime = 10 * 60000;
                if (accountSettings != null && accountSettings.getMinStopTime() != 0) {
                    stopTime = accountSettings.getMinStopTime() * 60000;
                }

                Object object = device.getAttrs().get("latestLocation");
                try {
                    DevicePosition lastLocation = objectMapper.readValue(objectMapper.writeValueAsString(object), DevicePosition.class);
                    if (lastLocation.getLocation() != null) {
                        List<Double> lastCoordinates = (List<Double>)((Map)lastLocation.getLocation()).get("coordinates");
                        if (lastCoordinates.get(0) == currentLocation.getLongitude() &&
                                lastCoordinates.get(1) == currentLocation.getLatitude()) {
                            LOG.info("Same as old location");
                            if (currentLocation.getDeviceTime() - lastLocation.getDeviceTime() > stopTime) {
                                currentLocation.setIdle(true);
                                currentLocation.setStopped(true);
                            } else {
                                currentLocation.setIdle(lastLocation.isIdle());
                                currentLocation.setStopped(lastLocation.isStopped());
                            }
                            currentLocation.setDistance(0);
                            currentLocation.setTotalDistance(lastLocation.getTotalDistance());
                        } else { //calculate the distance travelled
                            currentLocation.setIdle(false);
                            currentLocation.setStopped(false);
                            double lastLongitude = lastCoordinates.get(0);
                            double lastLatitude = lastCoordinates.get(1);
                            double currentLatitude = currentLocation.getLatitude();
                            double currentLongitude = currentLocation.getLongitude();
                            //position.distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((latitude-position.location.coordinates[1])*Math.PI/180 /2),2)+Math.cos(latitude*Math.PI/180)*Math.cos(position.location.coordinates[1]*Math.PI/180)*Math.pow(Math.sin((longitude-position.location.coordinates[0])*Math.PI/180/2),2)))
                            double distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((currentLatitude - lastLatitude) * Math.PI / 180 / 2), 2) + Math.cos(lastLatitude * Math.PI / 180) * Math.cos(currentLatitude * Math.PI / 180) * Math.pow(Math.sin((currentLongitude - lastLongitude) * Math.PI / 180 / 2), 2)));
                            currentLocation.setDistance(distance);
                            currentLocation.setTotalDistance(lastLocation.getTotalDistance() + distance);
                        }
                        devicePositionRepository.save(new DevicePosition());
                        mongoTemplate.save(currentLocation, "devicePositions");
                    } else {
                        LOG.info("no location was found in the last location");
                        KafkaController.createLocation(currentLocation);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(deviceService.updateLatestLocation(device.getImei(), currentLocation)){
                    LOG.info("processed deviceId:{}, totalDistance :{}, distance;{}, stopped:{}", currentLocation.getUniqueId(),
                            currentLocation.getTotalDistance(), currentLocation.getDistance(),
                            currentLocation.isStopped());
                }
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

        if(lastLocationJSON.containsKey("isIdle")){
            devicePosition.put("isIdle", (Boolean.parseBoolean(lastLocationJSON.get("isIdle").toString())));
        } else {
            devicePosition.put("isIdle", false);
        }
        if(lastLocationJSON.containsKey("isStopped")){
            devicePosition.put("isStopped", (Boolean.parseBoolean(lastLocationJSON.get("isStopped").toString())));
        } else {
            devicePosition.put("isStopped", false);
        }
        if(lastLocationJSON.containsKey("deviceTime")){
            devicePosition.put("deviceTime", (Double.parseDouble(lastLocationJSON.get("deviceTime").toString())));
        }

        if(lastLocationJSON.containsKey("location")){
            Map<String, Object>  loc = (Map<String, Object>)lastLocationJSON.get("location");
            devicePosition.put("location", loc);
        }
        return devicePosition;
    }
}
