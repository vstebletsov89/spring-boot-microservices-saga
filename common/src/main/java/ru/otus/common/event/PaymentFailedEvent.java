package ru.otus.common.event;

public record PaymentFailedEvent(
        String bookingId,
        String userId,
        String reason) {}