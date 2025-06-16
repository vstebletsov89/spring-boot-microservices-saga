package ru.otus.flight.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.flight.entity.BookingSeatMapping;
import ru.otus.common.entity.Flight;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.common.saga.SeatReservationFailedEvent;
import ru.otus.common.saga.SeatReservedEvent;
import ru.otus.flight.publisher.FlightPublisher;
import ru.otus.flight.repository.BookingSeatMappingRepository;
import ru.otus.flight.repository.FlightRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final FlightRepository flightRepository;
    private final BookingSeatMappingRepository mappingRepository;
    private final EventGateway eventGateway;
    private final FlightPublisher flightPublisher;

    @Transactional
    public void handle(ReserveSeatCommand cmd) {
        log.info("Attempting to reserve seat: {}", cmd);

        var flight = flightRepository
                .findByFlightNumberForUpdate(cmd.flightNumber())
                .orElseThrow(() ->
                        new RuntimeException("Flight not found: " + cmd.flightNumber())
                );

        BigDecimal freeSeats = calculateFreeSeats(flight);
        if (freeSeats.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No seats available for bookingId={}", cmd.bookingId());
            eventGateway.publish(new SeatReservationFailedEvent(cmd.bookingId()));
            return;
        }

        var existing =
                mappingRepository.findAllByFlightNumberForUpdate(cmd.flightNumber());

        String seatNumber = generateSeatNumber(existing);

        flight.setReservedSeats(flight.getReservedSeats() + 1);
        flightRepository.save(flight);
        publishFlightUpdatedEvent(flight);

        BookingSeatMapping seatMapping = BookingSeatMapping.builder()
                .bookingId(cmd.bookingId())
                .flightNumber(cmd.flightNumber())
                .seatNumber(seatNumber)
                .reservedAt(Instant.now())
                .build();
        mappingRepository.save(seatMapping);

        eventGateway.publish(new SeatReservedEvent(
                cmd.bookingId(),
                cmd.flightNumber(),
                cmd.userId(),
                flight.getPrice()
        ));

        log.info("Seat {} reserved for bookingId={}", seatNumber, cmd.bookingId());
    }

    public BigDecimal calculateFreeSeats(Flight flight) {
        // freeSeats = (totalSeats * (1 + overbookingPercentage / 100)) - bookedSeats
        BigDecimal totalSeats = BigDecimal.valueOf(flight.getTotalSeats());
        BigDecimal bookedSeats = BigDecimal.valueOf(flight.getReservedSeats());
        BigDecimal overbookFactor = BigDecimal.ONE.add(
                flight.getOverbookingPercentage()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        return totalSeats.multiply(overbookFactor).subtract(bookedSeats);
    }

    private String generateSeatNumber(List<BookingSeatMapping> existingMappings) {
        Set<String> reserved = existingMappings.stream()
                .map(BookingSeatMapping::getSeatNumber)
                .collect(Collectors.toSet());

        char[] letters = {'A','B','C','D','E','F','G','H','J','K'};
        for (int row = 1; row <= 50; row++) {
            for (char c : letters) {
                String seat = row + String.valueOf(c);
                if (!reserved.contains(seat)) {
                    return seat;
                }
            }
        }
        throw new IllegalStateException("No available seats");
    }

    @Transactional
    public void handle(ReleaseSeatCommand cmd) {
        log.info("Releasing seat for bookingId={}", cmd.bookingId());

        mappingRepository.findByBookingId(cmd.bookingId()).ifPresent(mapping -> {

            flightRepository.findByFlightNumberForUpdate(mapping.getFlightNumber())
                    .ifPresent(flight -> {
                        flight.setReservedSeats(Math.max(0, flight.getReservedSeats() - 1));
                        flightRepository.save(flight);
                        publishFlightUpdatedEvent(flight);
                    });

            mappingRepository.delete(mapping);
        });
    }

    private void publishFlightUpdatedEvent(Flight flight) {
        flightPublisher.publish(
                flight.getFlightNumber(),
                new FlightUpdatedEvent(
                        UUID.randomUUID().toString(),
                        flight.getFlightNumber(),
                        flight.getStatus(),
                        flight.getDepartureTime(),
                        flight.getArrivalTime(),
                        flight.getPrice(),
                        flight.getTotalSeats(),
                        flight.getReservedSeats(),
                        flight.getOverbookingPercentage()
                )
        );
    }
}