package ru.otus.reservation.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.kafka.ReservationCreatedEvent;
import ru.otus.reservation.cache.EventDeduplicationCache;
import ru.otus.reservation.publisher.DltPublisher;
import ru.otus.reservation.service.BookingProcessor;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTicketStreamProcessor {

    private final ObjectMapper objectMapper;
    private final BookingProcessor bookingProcessor;
    private final DltPublisher dltPublisher;
    private final EventDeduplicationCache eventDeduplicationCache;

    @Value("${app.kafka.topic.reservations}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> processReservations(StreamsBuilder builder)  {
        KStream<String, String> stream = builder.stream(topic);
        log.info("Kafka Streams is starting to listen to topic: {}", topic);

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
                    case "ReservationCreatedEvent" -> {
                        ReservationCreatedEvent event = objectMapper.readValue(value, ReservationCreatedEvent.class);
                        bookingProcessor.sendCreatedCommand(event);
                        log.info("Processed ReservationCreatedEvent: {}", event);
                    }
                    case "ReservationCancelledEvent" -> {
                        ReservationCancelledEvent event = objectMapper.readValue(value, ReservationCancelledEvent.class);
                        bookingProcessor.sendCancelledCommand(event);
                        log.info("Processed ReservationCancelledEvent: {}", event);
                    }
                    default -> log.warn("Unknown event type: {}", type);
                }

            } catch (Exception e) {
                log.error("Error processing message, sending to DLT: {}", value, e);
                dltPublisher.publish(dltTopic, key, value);
            }
        });

        return stream;
    }
}

