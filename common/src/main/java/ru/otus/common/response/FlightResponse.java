package ru.otus.common.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record FlightResponse(
        String flightNumber,
        String departureAirportCode,
        String arrivalAirportCode,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime,
        BigDecimal price) {}
