package ru.otus.common.events;

public record BookingCreatedEvent(
        String userId,
        String flightNumber,
        String bookingId) {}
