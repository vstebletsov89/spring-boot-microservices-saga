package ru.otus.flight.aggregate;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.common.saga.SeatReservedEvent;
import ru.otus.flight.service.SeatService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SeatAggregateTest {

    private AggregateTestFixture<SeatAggregate> fixture;
    private SeatService seatService;

    @BeforeEach
    void setUp() {
        seatService = mock(SeatService.class);
        fixture = new AggregateTestFixture<>(SeatAggregate.class);
        fixture.registerInjectableResource(seatService);
    }

    @Test
    void shouldReserveSeatSuccessfully() {
        var command = new ReserveSeatCommand("booking123", "FL1001", "1");

        fixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents();

        verify(seatService).handle(command);
    }

    @Test
    void shouldReleaseSeatSuccessfully() {
        var initialEvent = new SeatReservedEvent("booking123", "FL1001", "1", null);
        var command = new ReleaseSeatCommand("booking123");

        fixture.given(initialEvent)
                .when(command)
                .expectNoEvents();

        verify(seatService).handle(command);
    }

}