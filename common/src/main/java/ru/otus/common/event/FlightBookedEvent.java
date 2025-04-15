package ru.otus.common.event;

public record FlightBookedEvent(
        String bookingId,
        String userId,
        String flightNumber) {}