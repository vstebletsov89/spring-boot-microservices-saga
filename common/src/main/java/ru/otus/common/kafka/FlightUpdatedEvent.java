package ru.otus.common.kafka;

import ru.otus.common.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightUpdatedEvent(
        String eventId,
        String flightNumber,
        FlightStatus status,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        int totalSeats,
        int reservedSeats,
        BigDecimal overbookingPercentage) implements FlightEvent {}
