package ru.otus.common.event;

import ru.otus.common.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingEvent(
        String bookingId,
        String flightNumber,
        String seatNumber,
        OffsetDateTime reservedAt,
        BookingStatus status) {}
