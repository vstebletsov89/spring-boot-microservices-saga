package ru.otus.common.event;

public record BookingCreatedEvent(
        String userId,
        String flightNumber,
        String bookingId) {}
