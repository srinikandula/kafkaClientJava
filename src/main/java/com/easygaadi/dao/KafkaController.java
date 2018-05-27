package com.easygaadi.dao;

import com.easygaadi.kafka.consumer.Receiver;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kafka")
public final class KafkaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaController.class);

    private final TodoService service;

    @Autowired
    KafkaController(TodoService service) {
        this.service = service;
    }

    @Autowired
    private Sender sender;

    @Autowired
    private Receiver receiver;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    String send(@RequestBody @Valid JSONObject todoEntry) {
        LOGGER.info("sending", todoEntry.get("message"));
        sender.send(todoEntry.get("message").toString());
        return "Sent";
    }


    @RequestMapping(value = "/addDevicePosition", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.CREATED)
    String addDevicePosition(final HttpServletRequest request,@RequestParam String latitude) throws Exception {
        Map<String, String[]> requestParams = request.getParameterMap();
        LOGGER.debug("GET: request params {}", objectMapper.writeValueAsString(requestParams));
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
        BasicDBObject devicePosition = new BasicDBObject();
        devicePosition.put("gprmc", request.getParameter("gprmc"));
        devicePosition.put("name", request.getParameter("name"));
        devicePosition.put("uniqueId", request.getParameter("uniqueId"));
        devicePosition.put("deviceId", request.getParameter("deviceId"));
        devicePosition.put("protocol", request.getParameter("protocol"));
       if(request.getParameter("deviceTime") != null) {
            devicePosition.put("deviceTime", Double.parseDouble(request.getParameter("deviceTime")));
        }
        if(request.getParameter("fixTime") != null) {
            devicePosition.put("fixTime", Double.parseDouble(request.getParameter("fixTime")));
        }
        if(request.getParameter("valid") != null) {
            devicePosition.put("valid", Boolean.valueOf(request.getParameter("valid")));
        }

        if(request.getParameter("valid") != null && (
                request.getParameter("valid") != "true" || request.getParameter("valid") != "false")){
            devicePosition.put("latitude", Double.parseDouble(request.getParameter("valid")));
        } else if(request.getParameter("latitude") != null) {
            devicePosition.put("latitude", Double.parseDouble(request.getParameter("latitude")));
        }

        if(request.getParameter("longitude") != null) {
            devicePosition.put("longitude", Double.parseDouble(request.getParameter("longitude")));
        }
        if(request.getParameter("altitude") != null) {
            devicePosition.put("altitude", Double.parseDouble(request.getParameter("altitude")));
        }
        if(request.getParameter("speed") != null) {
            devicePosition.put("speed", Double.parseDouble(request.getParameter("speed")));
        }
        devicePosition.put("statusCode", request.getParameter("statusCode"));
        if(request.getParameter("course") != null) {
            devicePosition.put("course", Double.parseDouble(request.getParameter("course").toString()));
        }
        //devicePosition.setAttributes(request.getParameter("attributes"));
        devicePosition.put("address", request.getParameter("address"));
        if(request.getParameter("attributes") != null) {
            Map<String,Object> attributes = objectMapper.readValue(request.getParameter("attributes"), Map.class);
            devicePosition.put("attributes", attributes);
        }
        if(Double.parseDouble(devicePosition.get("latitude").toString()) == 0 ||
                Double.parseDouble(devicePosition.get("longitude").toString()) == 0){
            LOGGER.error("Found 0.0 location");
        } else {
            createLocation(devicePosition);
            String value =  objectMapper.writeValueAsString(devicePosition);
            sender.send(value);
        }

        return "Sent";
    }

    public static void createLocation(BasicDBObject devicePosition) {
        Map<String, Object> location = new HashMap<>();
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(Double.parseDouble(devicePosition.get("longitude").toString()));
        coordinates.add(Double.parseDouble(devicePosition.get("latitude").toString()));
        location.put("coordinates", coordinates);
        location.put("type","Point");
        devicePosition.put("location", location);
    }
}
