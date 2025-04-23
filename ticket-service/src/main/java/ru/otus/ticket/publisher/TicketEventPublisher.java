package ru.otus.ticket.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.ticket.entity.BookingOutboxEvent;
import ru.otus.ticket.repository.BookingOutboxRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final BookingOutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic.outbound}")
    private String topic;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<BookingOutboxEvent> events = repository.findTop50BySentFalseOrderByCreatedAtAsc();

        for (BookingOutboxEvent event : events) {
            kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            event.setSent(true);
                            repository.save(event);
                            log.info("Published pending event {}", event.getId());
                        } else {
                            log.error("Failed to publish event: {}", event.getId(), ex);
                        }
                    });
        }
    }
}
