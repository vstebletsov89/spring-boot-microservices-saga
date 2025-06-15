package ru.otus.orchestrator.saga;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.command.CancelFlightCommand;
import ru.otus.common.command.ConfirmBookingCommand;
import ru.otus.common.command.ReservationCancelledCommand;
import ru.otus.common.enums.CancellationStatus;
import ru.otus.common.saga.*;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Aggregate
@Slf4j
@Getter
public class BookingAggregate {

    @AggregateIdentifier
    private String bookingId;
    private boolean confirmed;
    private CancellationStatus cancellationStatus;

    public BookingAggregate() {}

    @CommandHandler
    public BookingAggregate(BookFlightCommand cmd) {
        log.info("Handling booking command: {}", cmd);
        apply(new BookingCreatedEvent(cmd.bookingId(), cmd.userId(), cmd.flightNumber(), cmd.seatNumber()));
    }

    @EventSourcingHandler
    public void on(BookingCreatedEvent event) {
        log.info("Handling flight booked event: {}", event);
        this.bookingId = event.bookingId();
        this.confirmed = false;
        this.cancellationStatus = CancellationStatus.NONE;
    }

    @CommandHandler
    public void handle(ConfirmBookingCommand cmd) {
        log.info("Handling confirm booking command: {}", cmd);
        if (this.confirmed) {
            log.warn("Booking {} already confirmed. Skipping.", cmd.bookingId());
            return;
        }
        apply(new BookingConfirmedEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingConfirmedEvent event) {
        log.info("Handling booking confirmed event: {}", event);
        this.confirmed = true;
    }

    @CommandHandler
    public void handle(ReservationCancelledCommand cmd) {
        log.info("Handling reservation cancelled command: {}", cmd);
        if (this.cancellationStatus != CancellationStatus.NONE) {
            log.warn("Booking {} already cancelled. Skipping.", cmd.bookingId());
            return;
        }
        apply(new BookingCancelledEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingCancelledEvent event) {
        log.info("Handling booking system cancelled event: {}", event);
        this.cancellationStatus = CancellationStatus.SYSTEM_CANCELLED;
    }

    @CommandHandler
    public void handle(CancelFlightCommand cmd) {
        log.info("Handling user cancelled command: {}", cmd);
        if (this.cancellationStatus != CancellationStatus.NONE) {
            log.warn("Booking {} already cancelled. Skipping.", cmd.bookingId());
            return;
        }
        apply(new BookingCancellationRequestedEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingCancellationRequestedEvent event) {
        log.info("Handling booking cancelled by user event: {}", event);
        this.cancellationStatus = CancellationStatus.USER_CANCELLED;
    }
}
