package ru.otus.common.events;

public record PaymentProcessedEvent(
        String bookingId,
        String userId) {}
