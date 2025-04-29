package ru.otus.common.response;

import ru.otus.common.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingSeatMappingResponse(
        String bookingId,
        String flightNumber,
        String seatNumber,
        OffsetDateTime reservedAt,
        BookingStatus status) {}
