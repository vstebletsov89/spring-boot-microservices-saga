package ru.otus.common.kafka;

public record ReservationCreatedEvent(
        String eventId,
        String userId,
        String flightNumber,
        String bookingId,
        String seatNumber) implements ReservationEvent {}
