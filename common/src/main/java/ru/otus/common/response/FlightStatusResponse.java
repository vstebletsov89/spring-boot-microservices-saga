package ru.otus.common.response;

import java.time.ZonedDateTime;

public record FlightStatusResponse(
        String flightNumber,
        String status,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime) {}
