package ru.otus.orchestrator.saga;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReservationCancelledCommand;
import ru.otus.common.saga.BookingCancellationRequestedEvent;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookingCancellationSagaTest {

    private BookingCancellationSaga saga;
    private CommandGateway commandGateway;

    @BeforeEach
    void setUp() {
        saga = new BookingCancellationSaga();
        commandGateway = mock(CommandGateway.class);
        saga.commandGateway = commandGateway;
    }

    @Test
    void shouldHandleBookingCancellationRequestedEventAndEndSaga() {
        String bookingId = UUID.randomUUID().toString();
        BookingCancellationRequestedEvent event = new BookingCancellationRequestedEvent(bookingId);

        try (MockedStatic<SagaLifecycle> sagaLifecycle = Mockito.mockStatic(SagaLifecycle.class)) {
            saga.on(event);

            verify(commandGateway).send(new ReleaseSeatCommand(bookingId));
            verify(commandGateway).send(new RefundPaymentCommand(bookingId));
            verify(commandGateway).send(new ReservationCancelledCommand(bookingId));
            sagaLifecycle.verify(SagaLifecycle::end);
        }
    }
}