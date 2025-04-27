package ru.otus.common.saga;

public record BookingCreatedEvent(
        String userId,
        String flightNumber,
        String bookingId) {}
