package ru.otus.common.saga;

public record BookingCancelledEvent(
        String bookingId) {}
