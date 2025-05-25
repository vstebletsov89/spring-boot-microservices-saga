package ru.otus.reservation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.saga.BookingCancellationRequestedEvent;
import ru.otus.common.kafka.ReservationCreatedEvent;
import ru.otus.reservation.entity.BookingOutboxEvent;
import ru.otus.reservation.repository.BookingOutboxRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final BookingOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private String extractPayload(Object event) {

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize booking request", e);
        }
        return payload;
    }

    @Transactional
    public void createBookingRequest(ReservationCreatedEvent bookingCreatedEvent) {

        BookingOutboxEvent event = BookingOutboxEvent.builder()
                .aggregateId(bookingCreatedEvent.bookingId())
                .payload(extractPayload(bookingCreatedEvent))
                .createdAt(Instant.now())
                .sent(false)
                .build();

        outboxRepository.save(event);
    }

    public void cancelBookingRequest(ReservationCancelledEvent reservationCancelledEvent) {

        BookingOutboxEvent event = BookingOutboxEvent.builder()
                .aggregateId(reservationCancelledEvent.bookingId())
                .payload(extractPayload(reservationCancelledEvent))
                .createdAt(Instant.now())
                .sent(false)
                .build();

        outboxRepository.save(event);
    }
}