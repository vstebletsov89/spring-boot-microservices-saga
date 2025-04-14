package ru.otus.booking.saga;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import ru.otus.common.commands.CancelBookingCommand;
import ru.otus.common.commands.ConfirmBookingCommand;
import ru.otus.common.commands.ReserveSeatCommand;
import ru.otus.common.events.FlightBookedEvent;
import ru.otus.common.events.SeatReservationFailedEvent;
import ru.otus.common.events.SeatReservedEvent;

@Saga
@Slf4j
public class BookingSaga {

    @Autowired
    private transient CommandGateway commandGateway;

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
        commandGateway.send(new ConfirmBookingCommand(event.bookingId()));
        SagaLifecycle.end();
    }

    @SagaEventHandler(associationProperty = "bookingId")
    public void on(SeatReservationFailedEvent event) {
        log.info("Reservation failed for: {}", event);
        commandGateway.send(new CancelBookingCommand(event.bookingId()));
        SagaLifecycle.end();
    }
}

