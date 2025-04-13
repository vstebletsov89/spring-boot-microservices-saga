package ru.otus.ticketservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.events.BookingCreatedEvent;
import ru.otus.ticketservice.entity.BookingOutboxEvent;
import ru.otus.ticketservice.repository.BookingOutboxRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

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
                .id(UUID.randomUUID())
                .aggregateType("Booking")
                .aggregateId(bookingCreatedEvent.bookingId())
                .type("BookingRequested")
                .payload(payload)
                .createdAt(OffsetDateTime.now())
                .sent(false)
                .build();

        outboxRepository.save(event);
    }
}