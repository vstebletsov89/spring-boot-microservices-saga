package ru.otus.reservation.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import ru.otus.common.saga.*;
import ru.otus.reservation.service.BookingSyncService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventsHandler {

    private final BookingSyncService bookingSyncService;

    @EventHandler
    public void on(ReservationCreatedEvent event) {
        log.info("Projecting ReservationCreatedEvent: {}", event);
        bookingSyncService.handleReservationCreated(event);
    }

    @EventHandler
    public void on(BookingConfirmedEvent event) {
        log.info("Projecting BookingConfirmedEvent: {}", event);
        bookingSyncService.handleBookingConfirmed(event);
    }

    @EventHandler
    public void on(BookingCancelledEvent event) {
        log.info("Projecting BookingCancelledEvent: {}", event);
        bookingSyncService.handleBookingCancelled(event);
    }

    @EventHandler
    public void on(BookingCancellationRequestedEvent event) {
        log.info("Projecting BookingCancellationRequestedEvent: {}", event);
        bookingSyncService.handleBookingCancellationRequested(event);
    }

    @EventHandler
    public void on(PaymentProcessedEvent event) {
        log.info("Projecting PaymentProcessedEvent: {}", event);
        bookingSyncService.handlePaymentProcessed(event);
    }
}
