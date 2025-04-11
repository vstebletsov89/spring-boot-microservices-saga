package ru.otus.common.events;

public record BookingCancelledEvent(
        String bookingId) {}
