package ru.otus.ticket.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.common.saga.BookingCreatedEvent;
import ru.otus.ticket.publisher.DltPublisher;
import ru.otus.ticket.service.BookingProcessor;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTicketStreamProcessor {

    private final ObjectMapper objectMapper;
    private final BookingProcessor bookingProcessor;
    private final DltPublisher dltPublisher;

    @Value("${app.kafka.topic.outbound}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    //TODO: fix all kafka consumers, add required table:
//    stream.foreach((key, value) -> {
//        try {
//            BookingCreatedEvent event = objectMapper.readValue(value, BookingCreatedEvent.class);
//            String eventId = event.getEventId(); // Предполагается, что есть уникальный идентификатор
//
//            // Проверка: если событие уже обработано — пропускаем
//            if (processedEventStore.exists(eventId)) {
//                log.info("Skipping duplicate event: {}", eventId);
//                return;
//            }
//
//            bookingProcessor.process(event);
//            processedEventStore.save(eventId); // Отмечаем, что событие обработано
//            log.info("Processed booking event: {}", event);
//
//        } catch (Exception e) {
//            log.error("Error processing message, sending to DLT: {}", value, e);
//            dltPublisher.publish(dltTopic, key, value);
//        }
//    });


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

