package ru.otus.common.events;

import java.math.BigDecimal;

public record FlightBookedEvent(
        String bookingId,
        String userId,
        String flightNumber,
        BigDecimal price) {}