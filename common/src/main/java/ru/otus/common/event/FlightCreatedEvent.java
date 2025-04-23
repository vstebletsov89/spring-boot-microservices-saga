package ru.otus.common.event;

import ru.otus.common.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record FlightCreatedEvent(
        String flightNumber,
        String departureAirportCode,
        String arrivalAirportCode,
        FlightStatus status,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime,
        BigDecimal price,
        int totalSeats,
        int reservedSeats,
        BigDecimal overbookingPercentage) implements FlightEvent {}
