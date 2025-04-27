package ru.otus.ticket.saga;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import ru.otus.common.command.CancelBookingCommand;
import ru.otus.common.command.ConfirmBookingCommand;
import ru.otus.common.command.ProcessPaymentCommand;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.common.saga.FlightBookedEvent;
import ru.otus.common.saga.PaymentFailedEvent;
import ru.otus.common.saga.PaymentProcessedEvent;
import ru.otus.common.saga.SeatReservationFailedEvent;
import ru.otus.common.saga.SeatReservedEvent;

@Saga
@Slf4j
public class BookingSaga {

    @Autowired
    transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "bookingId")
    public void on(FlightBookedEvent event) {
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
        commandGateway.send(new CancelBookingCommand(event.bookingId()));
        commandGateway.send(new ReleaseSeatCommand(event.bookingId()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(SeatReservationFailedEvent event) {
        log.info("Reservation failed for: {}", event);
        commandGateway.send(new CancelBookingCommand(event.bookingId()));
        SagaLifecycle.end();
    }
}

