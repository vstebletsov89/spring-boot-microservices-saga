package ru.otus.orchestrator.saga;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.command.CancelFlightCommand;
import ru.otus.common.command.ConfirmBookingCommand;
import ru.otus.common.command.ReservationCancelledCommand;
import ru.otus.common.saga.BookingCancellationRequestedEvent;
import ru.otus.common.saga.BookingCancelledEvent;
import ru.otus.common.saga.BookingConfirmedEvent;
import ru.otus.common.saga.ReservationCreatedEvent;

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
                .when(new BookFlightCommand(bookingId, "1", "FL123", "6B"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"));
    }

    @Test
    void shouldConfirmBookingOnConfirmBookingCommand() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"))
                .when(new ConfirmBookingCommand(bookingId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BookingConfirmedEvent(bookingId));
    }

    @Test
    void shouldCancelBookingOnReservationCancelledCommand() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"))
                .when(new ReservationCancelledCommand(bookingId))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BookingCancelledEvent(bookingId));
    }

    @Test
    void eventSourcingHandler_setsBookingIdAndConfirmedFalse() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));

        assertEquals("b123", aggregate.getBookingId());
        assertFalse(aggregate.isConfirmed());
    }

    @Test
    void eventSourcingHandler_setsConfirmedTrue() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));
        aggregate.on(new BookingConfirmedEvent("b123"));

        assertTrue(aggregate.isConfirmed());
    }

    @Test
    void eventSourcingHandler_setsCancelledTrue() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));
        aggregate.on(new BookingCancelledEvent("b123"));

        assertTrue(aggregate.isCancelled());
    }

    @Test
    void shouldRequestBookingCancellationOnCancelFlightCommand() {
        String bookingId = UUID.randomUUID().toString();

        fixture.given(new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"))
                .when(new CancelFlightCommand(bookingId, "1", "FL123"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BookingCancellationRequestedEvent(bookingId));
    }

    @Test
    void eventSourcingHandler_setsCancelledTrue_onBookingCancellationRequestedEvent() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));
        aggregate.on(new BookingCancellationRequestedEvent("b123"));

        assertTrue(aggregate.isCancelled());
    }
}