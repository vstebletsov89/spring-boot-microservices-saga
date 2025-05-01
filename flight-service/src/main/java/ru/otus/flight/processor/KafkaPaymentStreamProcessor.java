package ru.otus.flight.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.flight.publisher.DltPublisher;
import ru.otus.flight.service.PaymentSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPaymentStreamProcessor {

    private final ObjectMapper objectMapper;
    private final PaymentSyncService paymentSyncService;
    private final DltPublisher dltPublisher;

    @Value("${app.kafka.topic.payments}")
    private String topic;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @Bean
    public KStream<String, String> paymentStream(StreamsBuilder builder) {
        KStream<String, String> stream = builder.stream(topic);
        log.info("Kafka Payment Stream is listening to topic: {}", topic);

        stream.foreach((key, value) -> {
            try {
                PaymentEvent event = objectMapper.readValue(value, PaymentEvent.class);
                paymentSyncService.handle(event);
                log.info("Processed PaymentEvent: {}", event);
            } catch (Exception e) {
                log.error("Failed to process Kafka message: {}", value, e);
                dltPublisher.publish(dltTopic, key, value);
            }
        });

        return stream;
    }
}
