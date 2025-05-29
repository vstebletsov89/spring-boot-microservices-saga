package ru.otus.reservation.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DltPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String key, String message) {

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
        String timestamp = Instant.now().toString();
        record.headers().add(new RecordHeader("event-timestamp", timestamp.getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record)
                .thenAccept(result -> log.info("Sent to DLT: {}", message))
                .exceptionally(ex -> {
                    log.error("Failed to send to DLT", ex);
                    return null;
                });
    }
}

