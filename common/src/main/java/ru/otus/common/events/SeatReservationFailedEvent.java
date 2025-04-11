package ru.otus.common.events;

public record SeatReservationFailedEvent(
        String bookingId) {}
