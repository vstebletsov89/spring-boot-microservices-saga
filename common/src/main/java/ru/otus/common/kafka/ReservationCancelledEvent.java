package ru.otus.common.kafka;

public record ReservationCancelledEvent(
        String eventId,
        String userId,
        String flightNumber,
        String bookingId) implements ReservationEvent {}
