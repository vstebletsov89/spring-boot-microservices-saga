package ru.otus.ticketservice.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.ticketservice.entity.BookingOutboxEvent;
import ru.otus.ticketservice.repository.BookingOutboxRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final BookingOutboxRepository repository;
    private final KafkaProducer<String, String> producer;

    @Value("${app.kafka.topic.outbound}")
    private String topic;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<BookingOutboxEvent> events = repository.findTop50BySentFalseOrderByCreatedAtAsc();

        for (BookingOutboxEvent event : events) {
            producer.send(new ProducerRecord<>(topic,
                    event.getAggregateId(), event.getPayload()),
                    (metadata, exception) -> {
                if (exception == null) {
                    event.setSent(true);
                    repository.save(event);
                    log.info("Published pending event {}", event);
                } else {
                    log.info("Error during publishing event {} {}",
                            event,
                            exception.getMessage());
                }
            });
        }
    }
}
