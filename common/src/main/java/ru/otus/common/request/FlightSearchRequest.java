package ru.otus.common.request;

import java.time.ZonedDateTime;

public record FlightSearchRequest(
        String fromCode,
        String toCode,
        ZonedDateTime departureDate,
        ZonedDateTime returnDate,
        int passengerCount) {}