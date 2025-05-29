package ru.otus.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.kafka.ReservationCreatedEvent;
import ru.otus.reservation.entity.BookingOutboxEvent;
import ru.otus.reservation.repository.BookingOutboxRepository;
import ru.otus.reservation.util.PayloadUtil;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final BookingOutboxRepository outboxRepository;
    private final PayloadUtil payloadUtil;

    @Transactional
    public void createBookingRequest(ReservationCreatedEvent bookingCreatedEvent) {

        BookingOutboxEvent event = BookingOutboxEvent.builder()
                .aggregateId(bookingCreatedEvent.bookingId())
                .payload(payloadUtil.extractPayload(bookingCreatedEvent))
                .createdAt(Instant.now())
                .sent(false)
                .build();

        outboxRepository.save(event);
    }

    public void cancelBookingRequest(ReservationCancelledEvent reservationCancelledEvent) {

        BookingOutboxEvent event = BookingOutboxEvent.builder()
                .aggregateId(reservationCancelledEvent.bookingId())
                .payload(payloadUtil.extractPayload(reservationCancelledEvent))
                .createdAt(Instant.now())
                .sent(false)
                .build();

        outboxRepository.save(event);
    }
}