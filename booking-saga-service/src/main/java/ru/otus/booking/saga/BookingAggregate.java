package ru.otus.booking.saga;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import ru.otus.common.commands.BookFlightCommand;
import ru.otus.common.commands.CancelBookingCommand;
import ru.otus.common.commands.ConfirmBookingCommand;
import ru.otus.common.events.BookingCancelledEvent;
import ru.otus.common.events.BookingConfirmedEvent;
import ru.otus.common.events.FlightBookedEvent;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Slf4j
public class BookingAggregate {

    @AggregateIdentifier
    private String bookingId;
    private boolean confirmed;

    public BookingAggregate() {}

    @CommandHandler
    public BookingAggregate(BookFlightCommand cmd) {

        apply(new FlightBookedEvent(
                cmd.bookingId(),
                cmd.userId(),
                cmd.flightNumber()));
    }

    @EventSourcingHandler
    public void on(FlightBookedEvent event) {
        this.bookingId = event.bookingId();
        this.confirmed = false;
    }

    @CommandHandler
    public void handle(ConfirmBookingCommand cmd) {
        apply(new BookingConfirmedEvent(cmd.bookingId()));
    }

    @CommandHandler
    public void handle(CancelBookingCommand cmd) {
        apply(new BookingCancelledEvent(cmd.bookingId()));
    }

    @EventSourcingHandler
    public void on(BookingConfirmedEvent event) {
        this.confirmed = true;
    }
}

