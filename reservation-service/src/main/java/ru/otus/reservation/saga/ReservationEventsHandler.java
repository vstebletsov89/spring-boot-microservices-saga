package ru.otus.reservation.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.common.saga.*;
import ru.otus.reservation.publisher.DltPublisher;
import ru.otus.reservation.service.BookingSyncService;
import ru.otus.reservation.util.PayloadUtil;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventsHandler {

    private final BookingSyncService bookingSyncService;
    private final DltPublisher dltPublisher;
    private final PayloadUtil payloadUtil;

    @Value("${app.kafka.topic.dlt}")
    private String dltTopic;

    @EventHandler
    public void on(ReservationCreatedEvent event) {
        handleEvent("ReservationCreatedEvent", event.bookingId(), event, () ->
                bookingSyncService.handleReservationCreated(event));
    }

    @EventHandler
    public void on(BookingConfirmedEvent event) {
        handleEvent("BookingConfirmedEvent", event.bookingId(), event, () ->
                bookingSyncService.handleBookingConfirmed(event));
    }

    @EventHandler
    public void on(BookingCancelledEvent event) {
        handleEvent("BookingCancelledEvent", event.bookingId(), event, () ->
                bookingSyncService.handleBookingCancelled(event));
    }

    @EventHandler
    public void on(BookingCancellationRequestedEvent event) {
        handleEvent("BookingCancellationRequestedEvent", event.bookingId(), event, () ->
                bookingSyncService.handleBookingCancellationRequested(event));
    }

    @EventHandler
    public void on(PaymentProcessedEvent event) {
        handleEvent("PaymentProcessedEvent", event.bookingId(), event, () ->
                bookingSyncService.handlePaymentProcessed(event));
    }

    private <T> void handleEvent(String eventName, String key, T event, Runnable handler) {
        try {
            log.info("Handling {}: {}", eventName, event);
            handler.run();
        } catch (Exception e) {
            log.error("Error processing {}, sending to DLT: {}", eventName, event, e);
            dltPublisher.publish(dltTopic, key, payloadUtil.extractPayload(event));
        }
    }

}
