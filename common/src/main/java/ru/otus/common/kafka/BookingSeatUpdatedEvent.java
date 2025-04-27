package ru.otus.common.kafka;

import ru.otus.common.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingSeatUpdatedEvent(
        String bookingId,
        String flightNumber,
        String seatNumber,
        OffsetDateTime reservedAt,
        BookingStatus status) implements BookingSeatEvent {}
