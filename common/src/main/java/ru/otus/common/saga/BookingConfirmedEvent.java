package ru.otus.common.saga;

public record BookingConfirmedEvent(
        String bookingId) {}
