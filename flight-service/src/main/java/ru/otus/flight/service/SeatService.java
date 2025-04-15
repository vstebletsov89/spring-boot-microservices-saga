package ru.otus.flight.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.commands.ReleaseSeatCommand;
import ru.otus.common.commands.ReserveSeatCommand;
import ru.otus.common.events.SeatReservationFailedEvent;
import ru.otus.common.events.SeatReservedEvent;
import ru.otus.flight.entity.BookingSeatMapping;
import ru.otus.flight.entity.SeatInventory;
import ru.otus.flight.repository.BookingSeatMappingRepository;
import ru.otus.flight.repository.FlightRepository;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final FlightRepository flightRepository;
    private final BookingSeatMappingRepository mappingRepository;
    private final EventGateway eventGateway;

    @Transactional
    @CommandHandler
    public void handle(ReserveSeatCommand cmd) {
        log.info("Try to reserve seat: {}", cmd);
        var flight = flightRepository
                .findById(cmd.flightNumber())
                .orElseThrow(() -> new RuntimeException("No information about flight"));

        if (flight.getReservedSeats() <
                Math.floor(flight.getTotalSeats() *
                        (1.0 + flight.getOverbookingPercentage() / 100.0))) {

            flight.setReservedSeats(flight.getReservedSeats() + 1);
            flightRepository.save(flight);
            mappingRepository.save(new BookingSeatMapping(cmd.bookingId(), cmd.flightNumber()));
            log.info("Seat was reserved for: {}", cmd);
            eventGateway.publish(
                    new SeatReservedEvent(
                            cmd.bookingId(),
                            cmd.flightNumber(),
                            cmd.userId(),
                            inventory.getPrice()));
        } else {
            log.info("Reservation failed for: {}", cmd);
            eventGateway.publish(new SeatReservationFailedEvent(cmd.bookingId()));
        }
    }

    @Transactional
    @CommandHandler
    public void handle(ReleaseSeatCommand cmd) {
        log.info("Cancel seat reservation for: {}", cmd);
        mappingRepository.findByBookingId(cmd.bookingId())
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
