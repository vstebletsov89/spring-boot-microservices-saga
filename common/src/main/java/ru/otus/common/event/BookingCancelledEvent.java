package ru.otus.common.event;

public record BookingCancelledEvent(
        String bookingId) {}
