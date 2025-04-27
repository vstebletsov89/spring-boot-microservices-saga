package ru.otus.ticket.saga;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.command.CancelBookingCommand;
import ru.otus.common.command.ConfirmBookingCommand;
import ru.otus.common.saga.BookingCancelledEvent;
import ru.otus.common.saga.BookingConfirmedEvent;
import ru.otus.common.saga.FlightBookedEvent;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingAggregateTest {

    private AggregateTestFixture<BookingAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(BookingAggregate.class);
    }

    @Test
    void shouldBookFlightOnBookFlightCommand() {
        String bookingId = UUID.randomUUID().toString();
        fixture.givenNoPriorActivity()
                .when(new BookFlightCommand(bookingId, "1", "FL123"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new FlightBookedEvent(bookingId, "1", "FL123"));
    }

    @Test
    void shouldConfirmBookingOnConfirmBookingCommand() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(new FlightBookedEvent(bookingId, "1", "FL123"))
                .when(new ConfirmBookingCommand(bookingId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BookingConfirmedEvent(bookingId));
    }

    @Test
    void shouldCancelBookingOnCancelBookingCommand() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(new FlightBookedEvent(bookingId, "1", "FL123"))
                .when(new CancelBookingCommand(bookingId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BookingCancelledEvent(bookingId));
    }

    @Test
    void eventSourcingHandler_setsBookingIdAndConfirmedFalse() {
        BookingAggregate aggregate = new BookingAggregate();

        aggregate.on(new FlightBookedEvent("b123", "u1", "FL001"));

        assertEquals("b123", aggregate.getBookingId());
        assertFalse(aggregate.isConfirmed());
    }

    @Test
    void eventSourcingHandler_setsConfirmedTrue() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new FlightBookedEvent("b123", "u1", "FL001"));
        aggregate.on(new BookingConfirmedEvent("b123"));

        assertTrue(aggregate.isConfirmed());
    }
}