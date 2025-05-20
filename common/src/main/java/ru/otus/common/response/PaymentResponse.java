package ru.otus.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.otus.common.enums.PaymentStatus;

import java.time.Instant;

@Schema(description = "Payment response data")
public record PaymentResponse(

        @Schema(description = "User ID", example = "627f2329-589e-436b-bb36-4474b3a5cc8e")
        String userId,

        @Schema(description = "Payment status", example = "SUCCESS", implementation = PaymentStatus.class)
        PaymentStatus status,

        @Schema(description = "Failure reason if payment failed", example = "Insufficient funds")
        String failureReason,

        @Schema(description = "Timestamp when payment occurred", example = "2024-05-21T12:34:56.789Z")
        Instant occurredAt
) {}
