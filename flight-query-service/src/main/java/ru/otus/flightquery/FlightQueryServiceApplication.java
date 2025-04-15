package ru.otus.flightquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class FlightQueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightQueryServiceApplication.class, args);
    }

}
