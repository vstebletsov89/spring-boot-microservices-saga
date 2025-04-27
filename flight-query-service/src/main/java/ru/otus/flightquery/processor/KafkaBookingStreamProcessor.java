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
import ru.otus.common.event.BookingSeatCreatedEvent;
import ru.otus.common.event.BookingSeatUpdatedEvent;
import ru.otus.common.event.FlightCreatedEvent;
import ru.otus.common.event.FlightUpdatedEvent;
import ru.otus.flightquery.publisher.DltPublisher;
import ru.otus.flightquery.service.BookingSyncService;
import ru.otus.flightquery.service.FlightSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaBookingStreamProcessor {

    private final ObjectMapper objectMapper;
    private final BookingSyncService bookingSyncService;
    private final DltPublisher dltPublisher;

    @Value("${app.kafka.topic.bookings}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> bookingStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(topic);
        log.info("Kafka Booking Stream is listening to topic: {}", topic);

        stream.foreach((key, value) -> {
            try {
                JsonNode root = objectMapper.readTree(value);
                String type = root.get("type").asText();

                switch (type) {
                    case "BookingSeatCreatedEvent" -> {
                        BookingSeatCreatedEvent event = objectMapper.readValue(value, BookingSeatCreatedEvent.class);
                        bookingSyncService.handleCreated(event);
                        log.info("Processed BookingSeatCreatedEvent: {}", event);
                    }
                    case "BookingSeatUpdatedEvent" -> {
                        BookingSeatUpdatedEvent event = objectMapper.readValue(value, BookingSeatUpdatedEvent.class);
                        bookingSyncService.handleUpdated(event);
                        log.info("Processed BookingSeatUpdatedEvent: {}", event);
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
