package ru.otus.ticketservice.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
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

    @Bean
    public KStream<String, BookingCreatedEvent> kStream(StreamsBuilder builder)  {
        KStream<String, String> stream = builder.stream("ticket-service.outbound");

        stream.foreach((key, value) -> {
            try {
                BookingCreatedEvent event = objectMapper.readValue(value, BookingCreatedEvent.class);
                bookingProcessor.process(event); // Business logic
            } catch (Exception e) {
                log.error("Error processing message, sending to DLT: {}", value, e);
                dltPublisher.publish("ticket-service.dlt", key, value); // Manual DLT
            }
        });

        return stream;
    }
}

//@Bean
//public KStream<String, BookingCreatedEvent> kStream(StreamsBuilder builder) {
//    Serde<String> keySerde = Serdes.String();
//    JsonSerde<BookingCreatedEvent> valueSerde = new JsonSerde<>(BookingCreatedEvent.class, objectMapper);
//
//    KStream<String, BookingCreatedEvent> stream = builder.stream(outboundTopic, Consumed.with(keySerde, valueSerde));
//
//    stream.peek((key, event) -> {
//        try {
//            bookingProcessor.process(event);
//            log.info("Processed booking event: {}", event);
//        } catch (Exception e) {
//            log.error("Failed to process event: {}", event, e);
//            // Здесь можно отправить в DLT или выполнить повторную попытку вручную
//        }
//    });
//
//    return stream;
//}
