package ru.otus.common.saga;

public record BookingCancellationRequestedEvent(
        String bookingId) {}
