package ru.otus.common.kafka;

public record ReservationCancelledEvent(
        String userId,
        String flightNumber,
        String bookingId) implements ReservationEvent {}
