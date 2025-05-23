package ru.otus.orchestrator.saga;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.command.CancelBookingCommand;
import ru.otus.common.command.ConfirmBookingCommand;
import ru.otus.common.command.ReservationCancelledCommand;
import ru.otus.common.saga.BookingCancellationRequestedEvent;
import ru.otus.common.saga.BookingCancelledEvent;
import ru.otus.common.saga.BookingConfirmedEvent;
import ru.otus.common.saga.ReservationCreatedEvent;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Slf4j
@Getter
public class BookingAggregate {

    @AggregateIdentifier
    private String bookingId;
    private boolean confirmed;
    private boolean cancelled;
    //TODO: add metrics for dashboard

    public BookingAggregate() {}

    @CommandHandler
    public BookingAggregate(BookFlightCommand cmd) {
        log.info("Handling booking command: {}", cmd);
        apply(new ReservationCreatedEvent(
                cmd.bookingId(),
                cmd.userId(),
                cmd.flightNumber()));
    }

    @EventSourcingHandler
    public void on(ReservationCreatedEvent event) {
        log.info("Handling flight booked event: {}", event);
        this.bookingId = event.bookingId();
        this.confirmed = false;
        this.cancelled = false;
    }

    @CommandHandler
    public void handle(ConfirmBookingCommand cmd) {
        log.info("Handling confirm booking command: {}", cmd);
        apply(new BookingConfirmedEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingConfirmedEvent event) {
        log.info("Handling booking confirmed event: {}", event);
        this.confirmed = true;
    }

    @CommandHandler
    public void handle(CancelBookingCommand cmd) {
        log.info("Handling cancel booking command: {}", cmd);
        apply(new BookingCancellationRequestedEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingCancellationRequestedEvent event) {
        log.info("Handling booking cancellation requested event: {}", event);
        //TODO:???
    }

    @CommandHandler
    public void handle(ReservationCancelledCommand cmd) {
        log.info("Handling set status cancelled command: {}", cmd);
        apply(new BookingCancelledEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingCancelledEvent event) {
        log.info("Handling status cancelled event: {}", event);
        this.cancelled = true;
    }
}
