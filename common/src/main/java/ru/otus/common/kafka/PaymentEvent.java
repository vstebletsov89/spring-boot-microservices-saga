package ru.otus.common.kafka;

import ru.otus.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEvent(
        String eventId,
        String bookingId,
        String userId,
        BigDecimal amount,
        PaymentStatus status,
        String failureReason,
        Instant occurredAt) {}
