package ru.otus.orchestrator.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.otus.common.command.*;
import ru.otus.common.saga.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookingSagaTest {

    private BookingSaga saga;
    private CommandGateway commandGateway;

    @BeforeEach
    void setUp() {
        saga = new BookingSaga();
        commandGateway = mock(CommandGateway.class);
        saga.commandGateway = commandGateway;
    }

    @Test
    void shouldHandleBookingCreatedEvent() {
        String bookingId = UUID.randomUUID().toString();
        BookingCreatedEvent event = new BookingCreatedEvent(bookingId, "1", "FL123", "6B");

        try (MockedStatic<SagaLifecycle> sagaLifecycle = Mockito.mockStatic(SagaLifecycle.class)) {
            saga.on(event);

            sagaLifecycle.verify(() -> SagaLifecycle.associateWith("bookingId", bookingId));
            verify(commandGateway).send(new ReserveSeatCommand(bookingId, "FL123", "1"));
        }
    }

    @Test
    void shouldHandleSeatReservedEvent() {
        String bookingId = UUID.randomUUID().toString();
        SeatReservedEvent event = new SeatReservedEvent(bookingId, "FL123", "1", BigDecimal.valueOf(100));

        saga.on(event);

        verify(commandGateway).send(new ProcessPaymentCommand(
                bookingId,
                "1",
                BigDecimal.valueOf(100)
        ));
    }

    @Test
    void shouldHandlePaymentProcessedEventAndEndSaga() {
        String bookingId = UUID.randomUUID().toString();
        PaymentProcessedEvent event = new PaymentProcessedEvent(bookingId, "1");

        try (MockedStatic<SagaLifecycle> sagaLifecycle = Mockito.mockStatic(SagaLifecycle.class)) {
            saga.on(event);

            verify(commandGateway).send(new ConfirmBookingCommand(bookingId));
            sagaLifecycle.verify(SagaLifecycle::end);
        }
    }

    @Test
    void shouldHandlePaymentFailedEventAndEndSaga() {
        String bookingId = UUID.randomUUID().toString();
        PaymentFailedEvent event = new PaymentFailedEvent(bookingId, "1", "Declined");

        try (MockedStatic<SagaLifecycle> sagaLifecycle = Mockito.mockStatic(SagaLifecycle.class)) {
            saga.on(event);

            verify(commandGateway).send(new ReleaseSeatCommand(bookingId));
            verify(commandGateway).send(new ReservationCancelledCommand(bookingId));
            sagaLifecycle.verify(SagaLifecycle::end);
        }
    }

    @Test
    void shouldHandleSeatReservationFailedEventAndEndSaga() {
        String bookingId = UUID.randomUUID().toString();
        SeatReservationFailedEvent event = new SeatReservationFailedEvent(bookingId);

        try (MockedStatic<SagaLifecycle> sagaLifecycle = Mockito.mockStatic(SagaLifecycle.class)) {
            saga.on(event);

            verify(commandGateway).send(new ReservationCancelledCommand(bookingId));
            sagaLifecycle.verify(SagaLifecycle::end);
        }
    }
}