package ru.otus.seatservice.service;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.springframework.stereotype.Service;
import ru.otus.common.commands.ReleaseSeatCommand;
import ru.otus.common.commands.ReserveSeatCommand;
import ru.otus.common.events.SeatReservationFailedEvent;
import ru.otus.common.events.SeatReservedEvent;
import ru.otus.seatservice.entity.BookingSeatMapping;
import ru.otus.seatservice.entity.SeatInventory;
import ru.otus.seatservice.repository.BookingSeatMappingRepository;
import ru.otus.seatservice.repository.SeatInventoryRepository;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatInventoryRepository inventoryRepository;
    private final BookingSeatMappingRepository mappingRepository;

    @CommandHandler
    public void handle(ReserveSeatCommand cmd) {
        SeatInventory inventory = inventoryRepository
                .findById(cmd.flightNumber())
                .orElseThrow(() -> new RuntimeException("No information about flight"));

        if (inventory.getReservedSeats() <
                Math.floor(inventory.getTotalSeats() * 1.01)) {

            inventory.setReservedSeats(inventory.getReservedSeats() + 1);
            inventoryRepository.save(inventory);
            mappingRepository.save(new BookingSeatMapping(cmd.bookingId(), cmd.flightNumber()));

            apply(new SeatReservedEvent(cmd.bookingId()));
        } else {
            apply(new SeatReservationFailedEvent(cmd.bookingId()));
        }
    }

    @CommandHandler
    public void handle(ReleaseSeatCommand cmd) {
        mappingRepository.findById(cmd.bookingId())
                .flatMap(mapping ->
                        inventoryRepository
                                .findById(mapping.getFlightNumber()))
                .ifPresent(inventory -> {

            inventory.setReservedSeats(Math.max(0, inventory.getReservedSeats() - 1));
            inventoryRepository.save(inventory);
            mappingRepository.deleteById(cmd.bookingId());
        });
    }
}
