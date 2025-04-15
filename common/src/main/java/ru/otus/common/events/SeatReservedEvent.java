package ru.otus.common.events;

import java.math.BigDecimal;

public record SeatReservedEvent(
        String bookingId,
        String flightNumber,
        String userId,
        BigDecimal amount) {}
