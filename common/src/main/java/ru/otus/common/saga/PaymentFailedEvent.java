package ru.otus.common.saga;

public record PaymentFailedEvent(
        String bookingId,
        String userId,
        String reason) {}