package ru.otus.flight.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.enums.PaymentStatus;
import ru.otus.common.kafka.BookingSeatUpdatedEvent;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.flight.publisher.BookingPublisher;
import ru.otus.flight.repository.BookingSeatMappingRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSyncService {

    private final BookingSeatMappingRepository mappingRepository;
    private final BookingPublisher bookingPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void handle(PaymentEvent event) {
        BookingSeatMapping mapping = mappingRepository.findByBookingId(event.bookingId())
                .orElseThrow(() -> new RuntimeException(
                        "BookingSeatMapping not found for bookingId=" + event.bookingId()
                ));


        var newStatus = event.status() == PaymentStatus.SUCCESS
                ? BookingStatus.PAID
                : BookingStatus.CANCELLED;
        mapping.setStatus(newStatus);
        mapping.setReservedAt(event.occurredAt());

        mappingRepository.save(mapping);
        log.info("BookingSeatMapping saved: {}", mapping);

        BookingSeatUpdatedEvent bookingEvent = new BookingSeatUpdatedEvent(
                mapping.getBookingId(),
                mapping.getFlightNumber(),
                mapping.getSeatNumber(),
                mapping.getReservedAt(),
                mapping.getStatus()
        );

        bookingPublisher.publish(mapping.getBookingId(), bookingEvent);
        log.info("BookingSeatUpdatedEvent published: {}", bookingEvent);
    }
}