package ru.otus.flight.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.entity.Flight;
import ru.otus.common.kafka.BookingSeatCreatedEvent;
import ru.otus.common.kafka.BookingSeatUpdatedEvent;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.common.saga.SeatReservationFailedEvent;
import ru.otus.common.saga.SeatReservedEvent;
import ru.otus.flight.publisher.BookingPublisher;
import ru.otus.flight.publisher.FlightPublisher;
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
    private final FlightPublisher flightPublisher;
    private final BookingPublisher bookingPublisher;

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
        publishFlightUpdatedEvent(flight);

        BookingSeatMapping seatMapping = BookingSeatMapping.builder()
                .bookingId(cmd.bookingId())
                .flightNumber(cmd.flightNumber())
                .seatNumber(generateSeatNumber(flight.getFlightNumber()))
                .reservedAt(OffsetDateTime.now())
                .status(BookingStatus.RESERVED)
                .build();

        mappingRepository.save(seatMapping);
        bookingPublisher.publish(seatMapping.getBookingId(), new BookingSeatCreatedEvent(
                seatMapping.getBookingId(),
                seatMapping.getFlightNumber(),
                seatMapping.getSeatNumber(),
                seatMapping.getReservedAt(),
                seatMapping.getStatus()
        ));

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
                .ifPresent(seatMapping -> {

            var flightOpt = flightRepository.findById(seatMapping.getFlightNumber());
            flightOpt.ifPresent(flight -> {
                flight.setReservedSeats(Math.max(0, flight.getReservedSeats() - 1));
                flightRepository.save(flight);
                publishFlightUpdatedEvent(flight);
            });

            seatMapping.setStatus(BookingStatus.CANCELLED);
            mappingRepository.save(seatMapping);
            bookingPublisher.publish(seatMapping.getBookingId(), new BookingSeatUpdatedEvent(
                    seatMapping.getBookingId(),
                    seatMapping.getFlightNumber(),
                    seatMapping.getSeatNumber(),
                    seatMapping.getReservedAt(),
                    seatMapping.getStatus()
            ));
        });
    }

    private void publishFlightUpdatedEvent(Flight flight) {

        FlightUpdatedEvent event = new FlightUpdatedEvent(
                flight.getFlightNumber(),
                flight.getStatus(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice(),
                flight.getTotalSeats(),
                flight.getReservedSeats(),
                flight.getOverbookingPercentage()
        );

        flightPublisher.publish(flight.getFlightNumber(), event);
    }
}
