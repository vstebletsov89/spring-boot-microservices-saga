package ru.otus.common.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightResponse(
        String flightNumber,
        String departureAirportCode,
        String arrivalAirportCode,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price) {}
