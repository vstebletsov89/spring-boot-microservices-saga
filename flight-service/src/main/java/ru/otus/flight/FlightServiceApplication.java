package ru.otus.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@EntityScan(basePackages = {"ru.otus.common.entity", "ru.otus.flight.entity"})
@SpringBootApplication
public class FlightServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightServiceApplication.class, args);
    }

}
