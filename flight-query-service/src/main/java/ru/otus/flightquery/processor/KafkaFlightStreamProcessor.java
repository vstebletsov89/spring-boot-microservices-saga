package ru.otus.flightquery.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.common.kafka.FlightCreatedEvent;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.flightquery.cache.EventDeduplicationCache;
import ru.otus.flightquery.publisher.DltPublisher;
import ru.otus.flightquery.service.FlightSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaFlightStreamProcessor {

    private final ObjectMapper objectMapper;
    private final FlightSyncService flightSyncService;
    private final DltPublisher dltPublisher;
    private final EventDeduplicationCache eventDeduplicationCache;

    @Value("${app.kafka.topic.flights}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> flightStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(topic);
        log.info("Kafka Flight Stream is listening to topic: {}", topic);

        stream.foreach((key, value) -> {
            try {

                JsonNode root = objectMapper.readTree(value);
                JsonNode eventIdNode = root.get("eventId");

                if (eventIdNode == null || eventIdNode.asText().isEmpty()) {
                    log.warn("eventId is missing, skipping message: {}", value);
                    dltPublisher.publish(dltTopic, key, value);
                    return;
                }

                String eventId = eventIdNode.asText();
                if (eventDeduplicationCache.isDuplicate(eventId)) {
                    log.info("Duplicate event detected, skipping eventId={}", eventId);
                    return;
                }

                String type = root.get("type").asText();
                switch (type) {
                    case "FlightCreatedEvent" -> {
                        FlightCreatedEvent event = objectMapper.readValue(value, FlightCreatedEvent.class);
                        flightSyncService.handleCreated(event);
                        log.info("Processed FlightCreatedEvent: {}", event);
                    }
                    case "FlightUpdatedEvent" -> {
                        FlightUpdatedEvent event = objectMapper.readValue(value, FlightUpdatedEvent.class);
                        flightSyncService.handleUpdated(event);
                        log.info("Processed FlightUpdatedEvent: {}", event);
                    }
                    default -> log.warn("Unknown event type: {}", type);
                }
            } catch (Exception e) {
                log.error("Failed to process Kafka message: {}", value, e);
                dltPublisher.publish(dltTopic, key, value);
            }
        });

        return stream;
    }
}
