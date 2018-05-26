package com.easygaadi.dao;

import com.easygaadi.kafka.producer.Sender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/kafka")
final class KafkaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaController.class);

    private final TodoService service;

    @Autowired
    KafkaController(TodoService service) {
        this.service = service;
    }

    @Autowired
    private Sender sender;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    String send(@RequestBody @Valid JSONObject todoEntry) {
        LOGGER.info("sending", todoEntry.get("message"));
        sender.send(todoEntry.get("message").toString());
        return "Sent";
    }


    @RequestMapping(value = "/addDevicePosition", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    String addDevicePositionPOST(@RequestBody JSONObject position) {
        LOGGER.info("POST: request params ", position.toString());
        sender.send(position.toString());
        LOGGER.info("sending", position.toString());
        return "Sent";
    }

    @RequestMapping(value = "/addDevicePosition", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.CREATED)
    String addDevicePosition(final HttpServletRequest request,@RequestParam String latitude) throws IOException {
        Map<String, String[]> requestParams = request.getParameterMap();
        /*
        {"gprmc":["$GPRMC,090557.000,A,1823.0244,N,07950.0309,E,6.48,36.00,260518,,*31"],
        "name":["AP36TB5566"],
        "uniqueId":["358511021201599"],
        "deviceId":["3"],
        "protocol":["gt06"],
        "deviceTime":["1527326424524"],
        "fixTime":["1527325557000"],
        "valid":["true"],
        "latitude":["18.38374"],
        "longitude":["79.83384833333334"],
        "altitude":["0.0"],
        "speed":["6.479484"],
        "course":["36.0"],
        "statusCode":["0xF11C"],
        "attributes":["{\"status\":70,\"ignition\":true,\"charge\":true,\"blocked\":false,\"battery\":6,\"rssi\":4,\"distance\":0.0,\"totalDistance\":7.772386046E7,\"motion\":true}"],
        "address":["Kakatiya Thermal Power Project Main Rd, Gudadupalle, Telangana, IN"]}
         */
        DevicePosition devicePosition = new DevicePosition();
        devicePosition.setGprmc(request.getParameter("gprmc"));
        devicePosition.setName(request.getParameter("name"));
        devicePosition.setUniqueId(request.getParameter("uniqueId"));
        devicePosition.setDeviceId(request.getParameter("deviceId"));
        devicePosition.setProtocol(request.getParameter("protocol"));
        if(request.getParameter("deviceTime") != null) {
            devicePosition.setDeviceTime(Double.parseDouble(request.getParameter("deviceTime")));
        }
        if(request.getParameter("fixTime") != null) {
            devicePosition.setFixTime(Double.parseDouble(request.getParameter("fixTime")));
        }
        if(request.getParameter("valid") != null) {
            devicePosition.setValid(Boolean.valueOf(request.getParameter("valid")));
        }
        if(request.getParameter("latitude") != null) {
            devicePosition.setLatitude(Double.parseDouble(request.getParameter("latitude")));
        }
        if(request.getParameter("longitude") != null) {
            devicePosition.setLongitude(Double.parseDouble(request.getParameter("longitude")));
        }
        if(request.getParameter("altitude") != null) {
            devicePosition.setAltitude(Double.parseDouble(request.getParameter("altitude")));
        }
        if(request.getParameter("speed") != null) {
            devicePosition.setSpeed(Double.parseDouble(request.getParameter("speed")));
        }
        devicePosition.setStatusCode(request.getParameter("statusCode"));
        if(request.getParameter("course") != null) {
            devicePosition.setCourse(Double.parseDouble(request.getParameter("course")));
        }
        //devicePosition.setAttributes(request.getParameter("attributes"));
        devicePosition.setAddress(request.getParameter("address"));
        if(request.getParameter("attributes") != null) {
            Map<String,Object> attributes = objectMapper.readValue(request.getParameter("attributes"), Map.class);
            devicePosition.setAttrs(attributes);
        }
        LOGGER.info("GET: request params {}", objectMapper.writeValueAsString(requestParams));
        BasicDBObject position = new BasicDBObject();
        String value =  objectMapper.writeValueAsString(devicePosition);
        sender.send(value);
        LOGGER.info("sending: {}",value);
        return "Sent";
    }
}
