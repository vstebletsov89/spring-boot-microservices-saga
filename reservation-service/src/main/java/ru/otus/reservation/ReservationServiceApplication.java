package ru.otus.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafkaStreams
@EnableScheduling
@EntityScan(basePackages =
        {"ru.otus.common.entity",
        "ru.otus.reservation.entity",
        "org.axonframework.eventhandling.tokenstore.jpa"})
@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

}
