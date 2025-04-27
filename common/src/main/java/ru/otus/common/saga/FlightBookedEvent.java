package ru.otus.common.saga;

public record FlightBookedEvent(
        String bookingId,
        String userId,
        String flightNumber) {}