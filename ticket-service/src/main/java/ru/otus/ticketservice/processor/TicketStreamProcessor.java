package ru.otus.ticketservice.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.common.events.BookingCreatedEvent;
import ru.otus.ticketservice.publisher.DltPublisher;
import ru.otus.ticketservice.service.BookingProcessor;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketStreamProcessor {

    private final ObjectMapper objectMapper;
    private final BookingProcessor bookingProcessor;
    private final DltPublisher dltPublisher;

    @Value("${app.kafka.topic.outbound}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> kStream(StreamsBuilder builder)  {
        KStream<String, String> stream = builder.stream(topic);

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

