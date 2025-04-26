package ru.otus.common.request;

import java.time.LocalDateTime;

public record FlightSearchRequest(
        String fromCode,
        String toCode,
        LocalDateTime departureDate,
        LocalDateTime returnDate,
        int passengerCount) {}