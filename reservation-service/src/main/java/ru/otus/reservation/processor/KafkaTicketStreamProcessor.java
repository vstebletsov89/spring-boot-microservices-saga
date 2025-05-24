package ru.otus.reservation.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.common.saga.BookingCreatedEvent;
import ru.otus.reservation.publisher.DltPublisher;
import ru.otus.reservation.service.BookingProcessor;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTicketStreamProcessor {

    private final ObjectMapper objectMapper;
    private final BookingProcessor bookingProcessor;
    private final DltPublisher dltPublisher;

    @Value("${app.kafka.topic.reservations}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder)  {
        KStream<String, String> stream = builder.stream(topic);
        log.info("Kafka Streams is starting to listen to topic: {}", topic);

        stream.foreach((key, value) -> {
            try {
                BookingCreatedEvent event = objectMapper.readValue(value, BookingCreatedEvent.class);
                bookingProcessor.process(event);
                log.info("Processed booking event: {}", event);
            } catch (Exception e) {
                log.error("Error processing message, sending to DLT: {}", value, e);
                dltPublisher.publish(dltTopic, key, value);
            }
        });

        return stream;
    }
}

