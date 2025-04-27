package ru.otus.common.saga;

import java.math.BigDecimal;

public record SeatReservedEvent(
        String bookingId,
        String flightNumber,
        String userId,
        BigDecimal amount) {}
