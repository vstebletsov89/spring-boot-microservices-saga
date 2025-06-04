package ru.otus.common.saga;

public record BookingCreatedEvent(
        String bookingId,
        String userId,
        String flightNumber,
        String seatNumber) {}