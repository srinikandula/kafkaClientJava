package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * var devicePositions = new mongoose.Schema({
 *     gprmc: String,
 *     name: String,
 *     uniqueId: String,
 *     deviceId: String,
 *     protocol: String,
 *     deviceTime: Number,
 *     fixTime: Number,
 *     valid: Boolean,
 *     location: {
 *         type: {
 *             type: String,
 *             default: "Point"
 *         },
 *         coordinates: [Number] //[longitude(varies b/w -180 and 180 W/E), latitude(varies b/w -90 and 90 N/S)]
 *     },
 *     altitude: String,
 *     speed: String,
 *     course: String,
 *     statusCode: String,
 *     attributes: {},
 *     address: String,
 *     isIdle:Boolean,
 *     isStopped:Boolean,
 *     distance:{type:Number,default:0},
 *     totalDistance:{type:Number,default:0}
 *         // isViewed : Boolean
 * }, { timestamps: true, versionKey: false });
 */
@Getter
@Setter
@Document(collection = "devicePositions")
public class DevicePosition extends AbstractDocument {
    private String gprmc;
    private String name;
    private String uniqueId;
    private String deviceId;
    private String protocol;
    private double deviceTime;
    private double stopDuration;
    private double fixTime;
    private boolean valid;
    private double altitude;
    private double speed;
    private double course;
    private String statusCode;
    private String address;
    private boolean isIdle;
    private boolean isStopped;
    private double distance;
    private double totalDistance;
    private double longitude;
    private double latitude;

    private double time;
    private double date;
    private double timeSecond;
    private Map<String, Object> location;
    private Map<String, Object> attributes;
    private String accountId;
}
