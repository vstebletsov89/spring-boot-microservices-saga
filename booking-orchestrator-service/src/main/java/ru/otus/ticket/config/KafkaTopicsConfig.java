package ru.otus.ticket.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${app.kafka.topic.outbound}")
    private String outboundTopic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public NewTopic outboundTopic() {
        return TopicBuilder.name(outboundTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dltTopic() {
        return TopicBuilder
                .name(dltTopic)
                .partitions(1)
                .replicas(1).build();
    }
}
