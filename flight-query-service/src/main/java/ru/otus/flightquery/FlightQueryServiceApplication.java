package ru.otus.flightquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@EntityScan(basePackages = {"ru.otus.common.entity"})
@SpringBootApplication
public class FlightQueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightQueryServiceApplication.class, args);
    }

}
