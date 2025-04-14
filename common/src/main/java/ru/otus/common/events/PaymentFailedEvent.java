package ru.otus.common.events;

public record PaymentFailedEvent(
        String bookingId,
        String userId,
        String reason) {}