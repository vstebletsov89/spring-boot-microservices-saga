package ru.otus.common.event;

import java.math.BigDecimal;

public record SeatReservedEvent(
        String bookingId,
        String flightNumber,
        String userId,
        BigDecimal amount) {}
