package ru.otus.ticket.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DltPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message)
                .thenAccept(result ->
                        log.info("Sent to DLT: {}", message))
                .exceptionally(ex -> {
                    log.error("Failed to send to DLT", ex);
                    return null;
                });
    }
}

