package ru.otus.common.event;

public record SeatReservationFailedEvent(
        String bookingId) {}
