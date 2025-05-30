package ru.otus.orchestrator.saga;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.*;
import ru.otus.common.enums.CancellationStatus;
import ru.otus.common.saga.*;

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
    void shouldRequestBookingCancellationOnCancelFlightCommand() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"))
                .when(new CancelFlightCommand(bookingId, "1", "FL123"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new BookingCancellationRequestedEvent(bookingId));
    }

    @Test
    void eventSourcingHandler_setsInitialStateOnReservationCreated() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));

        assertEquals("b123", aggregate.getBookingId());
        assertFalse(aggregate.isConfirmed());
        assertEquals(CancellationStatus.NONE, aggregate.getCancellationStatus());
    }

    @Test
    void eventSourcingHandler_setsConfirmedTrue() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));
        aggregate.on(new BookingConfirmedEvent("b123"));

        assertTrue(aggregate.isConfirmed());
    }

    @Test
    void eventSourcingHandler_setsCancelledStatus_systemCancelled() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));
        aggregate.on(new BookingCancelledEvent("b123"));

        assertEquals(CancellationStatus.SYSTEM_CANCELLED, aggregate.getCancellationStatus());
    }

    @Test
    void eventSourcingHandler_setsCancelledStatus_userInitiated() {
        BookingAggregate aggregate = new BookingAggregate();
        aggregate.on(new ReservationCreatedEvent("b123", "u1", "FL001", "6B"));
        aggregate.on(new BookingCancellationRequestedEvent("b123"));

        assertEquals(CancellationStatus.USER_CANCELLED, aggregate.getCancellationStatus());
    }

    @Test
    void shouldNotEmitEventIfAlreadyCancelledByUser() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(
                        new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"),
                        new BookingCancellationRequestedEvent(bookingId)
                )
                .when(new CancelFlightCommand(bookingId, "1", "FL123"))
                .expectNoEvents();
    }

    @Test
    void shouldNotEmitEventIfAlreadyCancelledBySystem() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(
                        new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"),
                        new BookingCancelledEvent(bookingId)
                )
                .when(new CancelFlightCommand(bookingId, "1", "FL123"))
                .expectNoEvents();
    }

    @Test
    void shouldNotEmitEventIfAlreadyConfirmed() {
        String bookingId = UUID.randomUUID().toString();
        fixture.given(
                        new ReservationCreatedEvent(bookingId, "1", "FL123", "6B"),
                        new BookingConfirmedEvent(bookingId)
                )
                .when(new ConfirmBookingCommand(bookingId))
                .expectNoEvents();
    }
}