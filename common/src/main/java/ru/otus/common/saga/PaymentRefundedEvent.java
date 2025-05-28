package ru.otus.common.saga;

public record PaymentRefundedEvent(
        String bookingId) {}
