package ru.otus.common.dto;

import java.time.ZonedDateTime;

public record FlightListItem(
        String flightNumber,
        String origin,
        String destination,
        ZonedDateTime departureTime) {}
