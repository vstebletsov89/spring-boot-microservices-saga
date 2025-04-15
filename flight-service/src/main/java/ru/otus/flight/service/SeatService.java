package ru.otus.flight.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.commands.ReleaseSeatCommand;
import ru.otus.common.commands.ReserveSeatCommand;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.events.SeatReservationFailedEvent;
import ru.otus.common.events.SeatReservedEvent;
import ru.otus.flight.entity.BookingSeatMapping;
import ru.otus.flight.entity.Flight;
import ru.otus.flight.repository.BookingSeatMappingRepository;
import ru.otus.flight.repository.FlightRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

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
        log.info("Attempting to reserve seat: {}", cmd);

        var flight = flightRepository
                .findById(cmd.flightNumber())
                .orElseThrow(() -> new RuntimeException("Flight not found: " + cmd.flightNumber()));

        // freeSeats = totalSeats * (1.0 + (overbookingPercentage / 100.0))
        BigDecimal freeSeats = BigDecimal.valueOf(flight.getTotalSeats())
                .multiply(BigDecimal.ONE
                        .add(flight.getOverbookingPercentage()
                                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));


        if (BigDecimal.valueOf(flight.getReservedSeats()).compareTo(freeSeats) < 0) {
            reserveSeat(flight, cmd);
        } else {
            log.info("Reservation failed for: {}", cmd);
            eventGateway.publish(new SeatReservationFailedEvent(cmd.bookingId()));
        }
    }

    private void reserveSeat(Flight flight, ReserveSeatCommand cmd) {
        flight.setReservedSeats(flight.getReservedSeats() + 1);
        flightRepository.save(flight);

        BookingSeatMapping seatMapping = new BookingSeatMapping();
        seatMapping.setBookingId(cmd.bookingId());
        seatMapping.setFlightNumber(cmd.flightNumber());
        seatMapping.setSeatNumber(generateSeatNumber(flight.getFlightNumber()));
        seatMapping.setReservedAt(OffsetDateTime.now());
        seatMapping.setStatus(BookingStatus.RESERVED);
        mappingRepository.save(seatMapping);

        log.info("Seat reserved successfully for bookingId: {}", cmd.bookingId());
        eventGateway.publish(new SeatReservedEvent(
                cmd.bookingId(),
                cmd.flightNumber(),
                cmd.userId(),
                flight.getPrice()
        ));
    }

    private String generateSeatNumber(String flightNumber) {
        Set<String> reservedSeats = mappingRepository
                .findAllByFlightNumber(flightNumber)
                .stream()
                .map(BookingSeatMapping::getSeatNumber)
                .collect(Collectors.toSet());

        int rows = 30;
        char[] seatLetters = {'A', 'B', 'C', 'D', 'E', 'F'};

        for (int row = 1; row <= rows; row++) {
            for (char seat : seatLetters) {
                String seatNumber = seat + String.valueOf(row);
                if (!reservedSeats.contains(seatNumber)) {
                    return seatNumber;
                }
            }
        }

        throw new RuntimeException("No available seats");
    }

    @Transactional
    @CommandHandler
    public void handle(ReleaseSeatCommand cmd) {
        log.info("Release seat for: {}", cmd);

        mappingRepository.findByBookingId(cmd.bookingId())
                .ifPresent(mapping -> {

            var flightOpt = flightRepository.findById(mapping.getFlightNumber());
            flightOpt.ifPresent(flight -> {
                flight.setReservedSeats(Math.max(0, flight.getReservedSeats() - 1));
                flightRepository.save(flight);
            });

            mapping.setStatus(BookingStatus.CANCELLED);
            mappingRepository.save(mapping);
        });
    }
}
