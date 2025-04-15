package ru.otus.common.event;

public record PaymentProcessedEvent(
        String bookingId,
        String userId) {}
