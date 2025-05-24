package ru.otus.reservation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.saga.BookingCreatedEvent;
import ru.otus.reservation.entity.BookingOutboxEvent;
import ru.otus.reservation.repository.BookingOutboxRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final BookingOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createBookingRequest(BookingCreatedEvent bookingCreatedEvent) {

        String payload;
        try {
            payload = objectMapper.writeValueAsString(bookingCreatedEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize booking request", e);
        }

        BookingOutboxEvent event = BookingOutboxEvent.builder()
                .aggregateId(bookingCreatedEvent.bookingId())
                .payload(payload)
                .createdAt(Instant.now())
                .sent(false)
                .build();

        outboxRepository.save(event);
    }
}