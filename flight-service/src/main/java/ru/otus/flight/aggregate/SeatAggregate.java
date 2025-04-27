package ru.otus.flight.aggregate;


import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.common.saga.SeatReservedEvent;
import ru.otus.flight.service.SeatService;

@Aggregate
public class SeatAggregate {

    @AggregateIdentifier
    private String bookingId;

    public SeatAggregate() {}

    @CommandHandler
    public SeatAggregate(ReserveSeatCommand cmd, SeatService seatService) {
        seatService.handle(cmd);
    }

    @CommandHandler
    public void handle(ReleaseSeatCommand cmd, SeatService seatService) {
        seatService.handle(cmd);
    }

    @EventSourcingHandler
    public void on(SeatReservedEvent event) {
        this.bookingId = event.bookingId();
    }
}

