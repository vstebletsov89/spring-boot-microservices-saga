package ru.otus.common.saga;

public record SeatReservationFailedEvent(
        String bookingId) {}
