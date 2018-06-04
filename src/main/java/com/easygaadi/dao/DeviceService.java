package com.easygaadi.dao;

import com.mongodb.BasicDBObject;

import java.util.List;
import java.util.Map;

/**
 * This interface declares the methods that provides CRUD operations for
 * {@link Todo} objects.
 * @author Petri Kainulainen
 */
public interface DeviceService {

    /**
     * Creates a new dao entry.
     * @param device  The information of the created dao entry.
     * @return      The information of the created dao entry.
     */
    Device create(Device device);

    /**
     * Deletes a dao entry.
     * @param id    The id of the deleted dao entry.
     * @return      THe information of the deleted dao entry.
     * @throws TodoNotFoundException if no dao entry is found.
     */
    Device delete(String id);

    /**
     * Finds all dao entries.
     * @return      The information of all dao entries.
     */
    List<Device> findAll();

    /**
     * Finds a single dao entry.
     * @param id    The id of the requested dao entry.
     * @return      The information of the requested dao entry.
     * @throws TodoNotFoundException if no dao entry is found.
     */
    Device findById(String id);

    /**
     * Updates the information of a dao entry.
     * @param todo  The information of the updated dao entry.
     * @return      The information of the updated dao entry.
     * @throws TodoNotFoundException if no dao entry is found.
     */
    Device update(Device todo);

    Device findByImei(String imei);
    boolean updateLatestLocation(String deviceId, DevicePosition devicePosition);
    boolean updateLatestStatus(String deviceId, DevicePosition devicePosition);
}
