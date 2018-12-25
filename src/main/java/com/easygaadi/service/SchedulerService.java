package com.easygaadi.service;

import com.easygaadi.dao.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    @Value("${app.devicePositionsArchiveLimit}")
    private int archiveLimitDays;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DevicePositionRepository devicePositionRepository;

    @Autowired
    private ArchivedDevicePositionRepository archivedDevicePositionRepository;

    @Autowired
    private GeoFenceRepository geoFenceRepository;

    @Autowired
    private GeoFenceReportRepository geoFenceReportRepository;

    @Autowired
    private TruckRepository truckRepository;

    @Scheduled(cron = "*/30 * * * * *")
    //@Scheduled(fixedDelay = 5000)
    public void archiveDevicePositions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - archiveLimitDays);
        Query q = new Query();
        q.addCriteria(Criteria.where("createdAt").lte(calendar.getTime()));
        q.limit(5000);
        List<DevicePosition> devicePositions = mongoTemplate.find(q, DevicePosition.class);
        logger.info("archiving devicePositions before {}, count {}", calendar.getTime(), devicePositions.size());
        devicePositions.stream().forEach( devicePosition -> {
            archivedDevicePositionRepository.save(new ArchivedDevicePosition(devicePosition));
        });
        devicePositionRepository.deleteAll(devicePositions);
    }

    //@Scheduled(cron = "0 */30 * * * *")
    //@Scheduled(fixedDelay = 10000)
    public void updateGeofenceReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 2);
        Date end = calendar.getTime();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 30);
        Date start = calendar.getTime();
        logger.info("looking for device positions start:{} end:{}", start, end);
        List<Account> accounts = accountRepository.findByRouteConfigEnabled(true);
        accounts.stream().forEach(account -> {
            if(account.isRouteConfigEnabled()){
                List<Truck> trucks = truckRepository.findByAccountId(new ObjectId(account.getId()));
                logger.info("found trucks in account {}", trucks.size());
                if(trucks != null || trucks.size() > 0){
                    Map<String, String> deviceIdTruckRegMap = trucks.stream().collect(
                            Collectors.toMap(x -> x.getDeviceId(), x -> x.getRegistrationNo(),
                                    (deviceId1, deviceId2) -> {
                                        logger.info("Duplicate deviceId found {} in account {}", deviceId1, account.getUserName());
                                        return deviceId1;
                                    }));
                    logger.info("truck Map {}", deviceIdTruckRegMap);
                    List<GeoFence> geoFences = geoFenceRepository.findByAccountId(account.getId());
                    logger.info("found geofences in account {} size:{}" ,account.getId(), geoFences.size());
                    if(geoFences.size() > 0) {
                        geoFences.parallelStream().forEach(geoFence -> {
                            List<Criteria> match = new ArrayList<>();
                            Criteria criteria = new Criteria();
                            List<Double> coordinates = (List<Double>)geoFence.getGeoLocation().get("coordinates");
                            double raidus = 0.1;
                            //convert meters to kilometers
                            if(geoFence.getRadius() != 0){
                                raidus = geoFence.getRadius()/100;
                            }
                            if(coordinates.size() == 2) {
                                Point point = new Point(coordinates.get(1), coordinates.get(0));
                                logger.info("searching for GPS location with in range {} and {}", point.getX(), point.getY());
                                match.add(Criteria.where("createdAt").lte(end));
                                match.add(Criteria.where("createdAt").gte(start));
                                match.add(Criteria.where("accountId").is(account.getId()));
                                NearQuery nearQuery = NearQuery.near(point).maxDistance(new Distance(raidus, Metrics.KILOMETERS));
                                criteria.andOperator(match.toArray(new Criteria[match.size()]));
                                Aggregation agg = newAggregation(
                                        geoNear(nearQuery,  "distance"),
                                        match(criteria),
                                        group("uniqueId").max("createdAt")
                                                .as("end").min("createdAt").as("start"));

                                AggregationResults<Document> groupResults
                                        = mongoTemplate.aggregate(agg, DevicePosition.class, Document.class);
                                List<Document> results = groupResults.getMappedResults();
                                logger.info("found some reports {}", results.size());
                                List<GeoFenceReport> fenceReports = new ArrayList<>();
                                results.stream().forEach(result -> {
                                    logger.info("depot {} truck {} deviceId {} start {} end {}", geoFence.getName(),
                                            deviceIdTruckRegMap.get(result.getString("_id")),
                                            result.getString("_id"), result.getDate("start"),
                                            result.getDate("end"));
                                    fenceReports.add(new GeoFenceReport(account.getId(),result.getString("_id"),
                                            deviceIdTruckRegMap.get(result.getString("_id")),
                                            geoFence.getName(), result.getDate("start"),
                                            result.getDate("end")));
                                });
                                geoFenceReportRepository.saveAll(fenceReports);
                            }
                        });
                    }
                }
            }
        });

    }
}
