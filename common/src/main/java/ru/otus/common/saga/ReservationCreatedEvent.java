package ru.otus.common.saga;

public record ReservationCreatedEvent(
        String bookingId,
        String userId,
        String flightNumber) {}