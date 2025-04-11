package ru.otus.seatservice.aggregate;


import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import ru.otus.common.commands.ReleaseSeatCommand;
import ru.otus.common.commands.ReserveSeatCommand;
import ru.otus.common.events.SeatReservedEvent;
import ru.otus.seatservice.service.SeatService;

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

