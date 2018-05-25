package com.easygaadi.kafka;

import com.easygaadi.kafka.producer.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ProducerConsumerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ProducerConsumerApplication.class, args);
    }

    @Autowired
    private Sender sender;

    @Override
    public void run(String... strings) throws Exception {
        String data = "Spring Kafka Producer and Consumer Example is new";
       // sender.send(data);
    }



}