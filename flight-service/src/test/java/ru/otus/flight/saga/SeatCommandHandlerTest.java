package ru.otus.flight.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.flight.service.SeatService;

import static org.mockito.Mockito.*;

class SeatCommandHandlerTest {

    private SeatService seatService;
    private SeatCommandHandler seatCommandHandler;

    @BeforeEach
    void setUp() {
        seatService = mock(SeatService.class);
        seatCommandHandler = new SeatCommandHandler(seatService);
    }

    @Test
    void shouldHandleReserveSeatCommand() {
        var command = new ReserveSeatCommand("booking-123", "FL123", "12A");

        seatCommandHandler.handle(command);

        verify(seatService).handle(command);
        verifyNoMoreInteractions(seatService);
    }

    @Test
    void shouldHandleReleaseSeatCommand() {
        var command = new ReleaseSeatCommand("booking-456");

        seatCommandHandler.handle(command);

        verify(seatService).handle(command);
        verifyNoMoreInteractions(seatService);
    }
}