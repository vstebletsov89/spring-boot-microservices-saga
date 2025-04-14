package ru.otus.common.events;

public record SeatReservedEvent(
        String bookingId,
        String flightNumber,
        String userId,
        double amount) {}
