package ru.otus.ticketservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.ticketservice.service.BookingProcessor;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaStreamsConfig {

    private final KafkaProperties kafkaProperties;
    private final BookingProcessor bookingProcessor;
    private final ObjectMapper objectMapper;
    private final String applicationId;
    private final String outboundTopic;

    public KafkaStreamsConfig(
            KafkaProperties kafkaProperties,
            BookingProcessor bookingProcessor,
            ObjectMapper objectMapper,
            @Value("${spring.kafka.streams.application-id}") String applicationId,
            @Value("${app.kafka.topic.outbound}") String outboundTopic
    ) {
        this.kafkaProperties = kafkaProperties;
        this.bookingProcessor = bookingProcessor;
        this.objectMapper = objectMapper;
        this.applicationId = applicationId;
        this.outboundTopic = outboundTopic;
    }

    @Bean
    public StreamsConfig streamsConfig(SslBundles sslBundles) {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties(sslBundles);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        return new StreamsConfig(props);
    }

}
