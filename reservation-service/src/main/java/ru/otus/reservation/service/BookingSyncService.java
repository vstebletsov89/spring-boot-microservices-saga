package ru.otus.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.saga.*;
import ru.otus.reservation.entity.BookingInfo;
import ru.otus.reservation.repository.BookingRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingSyncService {

    private final BookingRepository bookingRepository;

    @Transactional
    public void handleReservationCreated(BookingCreatedEvent event) {

        bookingRepository.save(BookingInfo.builder()
                .bookingId(event.bookingId())
                .flightNumber(event.flightNumber())
                .seatNumber(event.seatNumber())
                .reservedAt(Instant.now())
                .status(BookingStatus.RESERVED)
                .build());
        log.info("Booking created: {}", event.bookingId());
    }

    @Transactional
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        bookingRepository.findByBookingId(event.bookingId()).ifPresent(mapping -> {
            mapping.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(mapping);
            log.info("Booking confirmed: {}", event.bookingId());
        });
    }

    @Transactional
    public void handleBookingCancelled(BookingCancelledEvent event) {
        bookingRepository.findByBookingId(event.bookingId()).ifPresent(mapping -> {
            mapping.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(mapping);
            log.info("Booking cancelled: {}", event.bookingId());
        });
    }

    @Transactional
    public void handleBookingCancellationRequested(BookingCancellationRequestedEvent event) {
        bookingRepository.findByBookingId(event.bookingId()).ifPresent(mapping -> {
            mapping.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(mapping);
            log.info("User requested booking cancellation: {}", event.bookingId());
        });
    }

    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        bookingRepository.findByBookingId(event.bookingId()).ifPresent(mapping -> {
            mapping.setStatus(BookingStatus.PAID);
            bookingRepository.save(mapping);
            log.info("Payment processed for booking: {}", event.bookingId());
        });
    }
}
