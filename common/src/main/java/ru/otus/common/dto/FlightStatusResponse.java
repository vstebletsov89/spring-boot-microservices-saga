package ru.otus.common.dto;

import java.time.ZonedDateTime;

public record FlightStatusResponse(
        String flightNumber,
        String status,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime) {}
