package com.easygaadi.kafka.consumer;


import com.easygaadi.controller.KafkaController;
import com.easygaadi.dao.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

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
       Device device = deviceService.findByImei(uniqueId);
        if(device == null){
            LOG.error("unknown position: {}", currentLocation);
        } else {
            if(device.getAccountId() != null) {
                currentLocation.setAccountId(device.getAccountId().toString());
            }
            if(device.getAttrs() == null || device.getAttrs().get("latestLocation") == null){
                LOG.warn("no last location was found");
                currentLocation.setDistance(0);
                currentLocation.setTotalDistance(0);
                KafkaController.createLocation(currentLocation);
                currentLocation = devicePositionRepository.save(currentLocation);
                //device.getAttrs().put("latestLocation", objectMapper.writeValueAsString(currentPosition));
                if(!deviceService.updateLatestLocation(device.getImei(), currentLocation)){
                    LOG.error("FAIL....");
                } else {
                    LOG.debug("Done...");
                }
            } else {
                //DevicePosition devicePosition = objectMapper.readValue(device.getAttrs().get("latestLocation"), DevicePosition.class);
                //LOG.error("address : {}", devicePosition.getAddress());
                GpsSettings accountSettings = accountGPSSettings.get(device.getAccountId());
                if(accountSettings == null){
                    accountSettings = gpsSettingsRepository.findByAccountId(device.getAccountId());
                    if(accountSettings != null && device.getAccountId() != null) {
                        accountGPSSettings.put(device.getAccountId().toString(), accountSettings);
                    }
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

                        double lastLongitude = lastCoordinates.get(0);
                        double lastLatitude = lastCoordinates.get(1);
                        double currentLatitude = currentLocation.getLatitude();
                        double currentLongitude = currentLocation.getLongitude();
                        //check if the location co-ordinates are same as the lastLocation
                        if(lastLongitude == currentLongitude && lastLatitude == currentLatitude) {
                            currentLocation.setSpeed(0);
                        }

                        //check if speed ==0 or motion == false
                        if (currentLocation.getSpeed() == 0) {
                            currentLocation.setIdle(true);
                            //compare to the lastHaltedTime to currentTime

                            if(device.getLastHaltedTime() != null){
                               //LOG.debug("Device {} halted since {} for {}", device.getImei(), device.getLastHaltedTime(), System.currentTimeMillis() - device.getLastHaltedTime().getMillis());
                               if(System.currentTimeMillis() - device.getLastHaltedTime().getMillis() > stopTime){
                                    currentLocation.setStopped(true);
                               }
                            }
                            //set last halted time on device if current speed is 0
                            if(lastLocation.isIdle() == false){
                                Update update = new Update();
                                update.set("lastHaltedTime", new DateTime());
                                update.set("attrs.latestLocation.isStopped", currentLocation.isStopped());
                                final Query query = new Query();
                                query.addCriteria(where("imei").is(device.getImei()));
                                UpdateResult updateResult =  mongoTemplate.updateMulti(query, update, Device.class);
                                if(updateResult.getModifiedCount() !=1){
                                    LOG.error("Failed to update lastHaltedTime on device {}-{} ", device.getImei());
                                } else {
                                    //LOG.debug("Updated lastHalted time to current time for device {}", device.getImei());
                                    device = deviceService.findByImei(uniqueId);
                                }
                            }
                            //IF speed is 0 do not calculate the distance
                            currentLocation.setTotalDistance(lastLocation.getTotalDistance());
                            currentLocation = devicePositionRepository.save(currentLocation);
                        } else {
                            if(lastLocation.isStopped()) {
                                //LOG.debug("Updating stopped time in the last location");
                                Update update = new Update();
                                update.set("stopDuration", System.currentTimeMillis() - device.getLastHaltedTime().getMillis());
                                final Query query = new Query();
                                query.addCriteria(where("_id").is(lastLocation.getId()));
                                UpdateResult updateResult =  mongoTemplate.updateMulti(query, update, DevicePosition.class);
                                /*if(updateResult.getModifiedCount() !=1){
                                    LOG.error("Failed to update stop time for uniqueId {}-{} ", lastLocation.getUniqueId(),lastLocation.getId());
                                } else {
                                    LOG.debug("Device started moving after {} - {}ms ",currentLocation.getUniqueId(), currentLocation.getDeviceTime() - lastLocation.getDeviceTime());
                                }*/
                            }
                            currentLocation.setIdle(false);
                            currentLocation.setStopped(false);
                            //calculate the distance travelled

                            //position.distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((latitude-position.location.coordinates[1])*Math.PI/180 /2),2)+Math.cos(latitude*Math.PI/180)*Math.cos(position.location.coordinates[1]*Math.PI/180)*Math.pow(Math.sin((longitude-position.location.coordinates[0])*Math.PI/180/2),2)))
                            double distance = 1.609344 * 3956 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin((currentLatitude - lastLatitude) * Math.PI / 180 / 2), 2) + Math.cos(lastLatitude * Math.PI / 180) * Math.cos(currentLatitude * Math.PI / 180) * Math.pow(Math.sin((currentLongitude - lastLongitude) * Math.PI / 180 / 2), 2)));
                            currentLocation.setDistance(distance);
                            currentLocation.setTotalDistance(lastLocation.getTotalDistance() + distance);
                            currentLocation = devicePositionRepository.save(currentLocation);
                        }
                    } else {
                        LOG.warn("no location was found in GPS Data");
                        KafkaController.createLocation(currentLocation);
                    }
                    if(deviceService.updateLatestLocation(device.getImei(), currentLocation)){
                       /* LOG.info("processed deviceId:{}, totalDistance :{}, distance;{}, stopped:{}", currentLocation.getUniqueId(),
                                currentLocation.getTotalDistance(), currentLocation.getDistance(),
                                currentLocation.isStopped());*/
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

}
