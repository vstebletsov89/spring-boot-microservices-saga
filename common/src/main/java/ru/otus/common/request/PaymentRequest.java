package ru.otus.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Payment request data")
public record PaymentRequest(

        @NotBlank(message = "Booking ID must not be blank")
        @Size(max = 36, message = "Booking ID must be at most 36 characters")
        @Schema(description = "Booking ID", example = "9a7759be-8b7a-4e27-a10a-52a0e2b7a7ce")
        String bookingId,

        @NotBlank(message = "User ID must not be blank")
        @Size(max = 36, message = "User ID must be at most 36 characters")
        @Schema(description = "User ID", example = "627f2329-589e-436b-bb36-4474b3a5cc8e")
        String userId,

        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        @Schema(description = "Payment amount", example = "1999.99")
        BigDecimal amount
) {}
