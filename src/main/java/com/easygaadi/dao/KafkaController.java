package com.easygaadi.dao;

import com.easygaadi.kafka.producer.Sender;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    String send(@RequestBody @Valid JSONObject todoEntry) {
        LOGGER.info("sending", todoEntry.get("message"));
        sender.send(todoEntry.get("message").toString());
        return "Sent";
    }


    @RequestMapping(value = "/addDevicePosition", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    String addDevicePosition(@RequestBody JSONObject position) {
        sender.send(position.toString());
        LOGGER.info("sending", position.toString());
        return "Sent";
    }
}
