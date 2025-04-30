package ru.otus.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${app.kafka.topic.payments}")
    private String paymentsTopic;

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder.name(paymentsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
