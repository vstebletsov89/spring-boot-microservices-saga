package ru.otus.seatservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Slf4j
public class SeatService {

    private final SeatInventoryRepository inventoryRepository;
    private final BookingSeatMappingRepository mappingRepository;

    @Transactional
    @CommandHandler
    public void handle(ReserveSeatCommand cmd) {
        log.info("Try to reserve seat: {}", cmd);
        SeatInventory inventory = inventoryRepository
                .findById(cmd.flightNumber())
                .orElseThrow(() -> new RuntimeException("No information about flight"));

        if (inventory.getReservedSeats() <
                Math.floor(inventory.getTotalSeats() *
                        (1.0 + inventory.getOverbookingPercentage() / 100.0))) {

            inventory.setReservedSeats(inventory.getReservedSeats() + 1);
            inventoryRepository.save(inventory);
            mappingRepository.save(new BookingSeatMapping(cmd.bookingId(), cmd.flightNumber()));
            log.info("Seat was reserved for: {}", cmd);
            apply(new SeatReservedEvent(cmd.bookingId()));
        } else {
            log.info("Reservation failed for: {}", cmd);
            apply(new SeatReservationFailedEvent(cmd.bookingId()));
        }
    }

    @Transactional
    @CommandHandler
    public void handle(ReleaseSeatCommand cmd) {
        log.info("Cancel seat reservation for: {}", cmd);
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
