package ru.otus.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafkaStreams
@EnableScheduling
@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

}
