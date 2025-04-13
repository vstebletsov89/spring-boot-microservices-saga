package ru.otus.ticketservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.common.events.BookingCreatedEvent;
import ru.otus.ticketservice.service.BookingProcessor;

import java.util.Map;

@Slf4j
@Configuration
@AllArgsConstructor
public class KafkaStreamsConfig {

    private final KafkaProperties kafkaProperties;
    private final BookingProcessor bookingProcessor;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${app.kafka.topic.outbound}")
    private String outboundTopic;

    @Bean
    public StreamsConfig streamsConfig(SslBundles sslBundles) {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties(sslBundles);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        return new StreamsConfig(props);
    }

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(outboundTopic);

        stream.peek((key, value) -> {
            try {
                BookingCreatedEvent event = objectMapper.readValue(value, BookingCreatedEvent.class);
                bookingProcessor.process(event);
                log.info("request processed: {}",  event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process message", e);
            }
        });

        return stream;
    }
}
