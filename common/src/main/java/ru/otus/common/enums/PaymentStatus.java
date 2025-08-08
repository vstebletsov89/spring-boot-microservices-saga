package ru.otus.common.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING("Payment pending: booking=%s, amount=%s"),
    SUCCESS("Payment successful: booking=%s, amount=%s"),
    FAILED("Payment failed: booking=%s, reason=%s"),
    REFUNDED("Payment refunded: booking=%s, amount=%s"),
    REFUND_FAILED("Payment refund failed: booking=%s, reason=%s");

    private final String messageTemplate;

    PaymentStatus(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }
}