package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
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
@Document(collection = "archivedDevicePositions")
public class ArchivedDevicePosition extends DevicePosition {
    public ArchivedDevicePosition(DevicePosition devicePosition){
        BeanUtils.copyProperties(devicePosition, this);
    }
}
