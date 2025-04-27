package ru.otus.ticket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;
import ru.otus.common.command.BookFlightCommand;
import ru.otus.common.saga.BookingCreatedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingProcessor {

    private final CommandGateway commandGateway;

    public void process(BookingCreatedEvent event) {
        BookFlightCommand command = new BookFlightCommand(
                event.bookingId(),
                event.userId(),
                event.flightNumber()
        );

        commandGateway.send(command).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Failed to send command: {}", command, ex);
            } else {
                log.info("Successfully processed booking command: {}", command);
            }
        });
    }
}
