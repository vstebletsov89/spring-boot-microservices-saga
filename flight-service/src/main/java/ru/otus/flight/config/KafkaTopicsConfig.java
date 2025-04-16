package ru.otus.flight.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${app.kafka.topic.flights}")
    private String flightsTopic;

    @Bean
    public NewTopic flightsTopic() {
        return TopicBuilder.name(flightsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
