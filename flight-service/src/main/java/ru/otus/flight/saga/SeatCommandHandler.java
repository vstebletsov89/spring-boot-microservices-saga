package ru.otus.flight.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Component;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.flight.service.SeatService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatCommandHandler {

    private final SeatService seatService;

    @CommandHandler
    public void handle(ReserveSeatCommand cmd) {
        log.info("Handling ReserveSeatCommand: {}", cmd);
        seatService.handle(cmd);
    }

    @CommandHandler
    public void handle(ReleaseSeatCommand cmd) {
        log.info("Handling ReleaseSeatCommand: {}", cmd);
        seatService.handle(cmd);
    }
}
