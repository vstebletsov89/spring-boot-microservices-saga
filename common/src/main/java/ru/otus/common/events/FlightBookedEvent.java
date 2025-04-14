package ru.otus.common.events;

public record FlightBookedEvent(
        String bookingId,
        String userId,
        String flightNumber) {}