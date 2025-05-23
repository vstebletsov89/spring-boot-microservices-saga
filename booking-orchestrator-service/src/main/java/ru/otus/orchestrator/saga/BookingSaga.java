package ru.otus.orchestrator.saga;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import ru.otus.common.command.*;
import ru.otus.common.saga.*;

@Saga
@Slf4j
public class BookingSaga {

    @Autowired
    transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "bookingId")
    public void on(ReservationCreatedEvent event) {
        log.info("Try to reserve seat for: {}", event);
        SagaLifecycle.associateWith("bookingId", event.bookingId());
        commandGateway.send(new ReserveSeatCommand(
                event.bookingId(),
                event.flightNumber(),
                event.userId()));
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(SeatReservedEvent event) {
        log.info("Seat reserved for: {}", event);
        commandGateway.send(new ProcessPaymentCommand(
                event.bookingId(),
                event.userId(),
                event.amount()
        ));
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(PaymentProcessedEvent event) {
        log.info("Payment successful: {}", event);
        commandGateway.send(new ConfirmBookingCommand(event.bookingId()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(PaymentFailedEvent event) {
        log.warn("Payment failed: {}", event);
        commandGateway.send(new ReleaseSeatCommand(event.bookingId()));
        commandGateway.send(new ReservationCancelledCommand(event.bookingId()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(SeatReservationFailedEvent event) {
        log.info("Seat reservation failed for: {}", event);
        commandGateway.send(new ReservationCancelledCommand(event.bookingId()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(BookingCancellationRequestedEvent event) {
        log.info("User requested booking cancellation: {}", event);
        commandGateway.send(new ReleaseSeatCommand(event.bookingId()));
        commandGateway.send(new RefundPaymentCommand(event.bookingId()));
        commandGateway.send(new ReservationCancelledCommand(event.bookingId()));
        SagaLifecycle.end();
    }
}


