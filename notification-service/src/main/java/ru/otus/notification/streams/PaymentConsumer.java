package ru.otus.notification.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.notification.dlt.DltPublisher;
import ru.otus.notification.mapper.NotificationOutboxMapper;
import ru.otus.notification.repository.NotificationOutboxRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationOutboxMapper mapper;
    private final NotificationOutboxRepository repository;
    private final DltPublisher dltPublisher;
    private final Executor writeExecutor;

    @Value("${app.kafka.topic.payments}")
    private String paymentsTopic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> paymentStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(paymentsTopic);
        log.info("Kafka Payment Stream is listening to topic: {}", paymentsTopic);

        stream.foreach((key, value) -> {
            try {
                log.info("Received Payment Event: {}", objectMapper.writeValueAsString(value));
                PaymentEvent event = objectMapper.readValue(value, PaymentEvent.class);
                var outbox = mapper.toOutbox(event);
                CompletableFuture
                        .runAsync(() -> repository.save(outbox), writeExecutor)
                        .exceptionally(ex -> {
                            log.error("Async save failed for outbox id={}, eventId={}", outbox.getId(), outbox.getEventId(), ex);
                            dltPublisher.publish(dltTopic, key, value);
                            return null;
                        });

            } catch (Exception ex) {
                log.error("Failed to process Kafka message: {}", value, ex);
                dltPublisher.publish(dltTopic, key, value);
            }
        });

        return stream;
    }
}
