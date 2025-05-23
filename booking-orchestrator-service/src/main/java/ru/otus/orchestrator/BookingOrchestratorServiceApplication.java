package ru.otus.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableKafkaStreams
@SpringBootApplication
public class BookingOrchestratorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingOrchestratorServiceApplication.class, args);
	}

}
