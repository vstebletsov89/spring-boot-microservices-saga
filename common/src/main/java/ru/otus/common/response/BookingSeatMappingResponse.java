package ru.otus.common.response;

import ru.otus.common.enums.BookingStatus;

import java.time.Instant;

public record BookingSeatMappingResponse(
        String bookingId,
        String flightNumber,
        String seatNumber,
        Instant reservedAt,
        BookingStatus status) {}
