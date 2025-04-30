package ru.otus.common.kafka;

import ru.otus.common.enums.BookingStatus;

import java.time.Instant;

public record BookingSeatUpdatedEvent(
        String bookingId,
        String flightNumber,
        String seatNumber,
        Instant reservedAt,
        BookingStatus status) implements BookingSeatEvent {}
