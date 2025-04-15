package ru.otus.common.request;

import java.time.ZonedDateTime;

public record FlightSearchRequest(
        String fromCity,
        String toCity,
        ZonedDateTime departureDate,
        ZonedDateTime returnDate,
        int passengers) {}