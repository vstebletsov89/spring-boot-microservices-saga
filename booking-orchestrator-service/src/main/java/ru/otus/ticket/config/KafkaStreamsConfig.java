package ru.otus.ticket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${spring.kafka.streams.default-key-serde}")
    private String defaultKeySerde;

    @Value("${spring.kafka.streams.default-value-serde}")
    private String defaultValueSerde;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, applicationId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, defaultKeySerde);
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, defaultValueSerde);

        return new KafkaStreamsConfiguration(props);
    }
}
