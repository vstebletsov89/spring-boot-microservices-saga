package ru.otus.common.saga;

public record PaymentProcessedEvent(
        String bookingId,
        String userId) {}
