package ru.otus.orchestrator.saga;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import ru.otus.common.command.RefundPaymentCommand;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReservationCancelledCommand;
import ru.otus.common.saga.BookingCancellationRequestedEvent;
import ru.otus.orchestrator.metrics.BookingMetrics;

@Saga
@Slf4j
public class BookingCancellationSaga {

    @Autowired
    transient CommandGateway commandGateway;

    @Autowired
    private transient BookingMetrics bookingMetrics;

    @StartSaga
    @SagaEventHandler(associationProperty = "bookingId")
    public void on(BookingCancellationRequestedEvent event) {
        log.info("User requested booking cancellation: {}", event);
        if (bookingMetrics != null) bookingMetrics.incrementCancelled();

        SagaLifecycle.associateWith("bookingId", event.bookingId());
        commandGateway.send(new ReleaseSeatCommand(event.bookingId()));
        commandGateway.send(new RefundPaymentCommand(event.bookingId()));
        commandGateway.send(new ReservationCancelledCommand(event.bookingId()));
        SagaLifecycle.end();
    }
}
