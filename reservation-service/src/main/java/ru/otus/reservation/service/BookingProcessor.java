package ru.otus.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.command.CancelFlightCommand;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.kafka.ReservationCreatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingProcessor {

    private final CommandGateway commandGateway;

    private void sendCommandToSaga(Object command) {
        commandGateway.send(command).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Failed to send command: {}", command, ex);
            } else {
                log.info("Successfully processed booking command: {}", command);
            }
        });
    }

    public void sendCreatedCommand(ReservationCreatedEvent event) {
        sendCommandToSaga(new BookFlightCommand(
                event.bookingId(),
                event.userId(),
                event.flightNumber(),
                event.seatNumber()
        ));
    }

    public void sendCancelledCommand(ReservationCancelledEvent event) {
        sendCommandToSaga(new CancelFlightCommand(
                event.bookingId(),
                event.userId(),
                event.flightNumber()
        ));
    }
}
