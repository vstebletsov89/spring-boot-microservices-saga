package ru.otus.common.response;

import java.time.LocalDateTime;

public record FlightStatusResponse(
        String flightNumber,
        String status,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime) {}
