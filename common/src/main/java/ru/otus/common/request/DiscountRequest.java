package ru.otus.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request payload for discount calculation")
public record DiscountRequest(

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
        @Schema(description = "Original ticket price before discounts", example = "499.99")
        BigDecimal basePrice,

        @NotNull(message = "Booking date is required")
        @Schema(description = "Date when the ticket was booked", example = "2025-07-01")
        LocalDate bookingDate,

        @NotNull(message = "Departure date is required")
        @Schema(description = "Date when the flight is scheduled to depart", example = "2025-08-15")
        LocalDate departureDate,

        @Schema(description = "Flag indicating if the passenger is a student", example = "true")
        Boolean isStudent,

        @Min(value = 0, message = "Completed bookings cannot be negative")
        @Schema(description = "Number of previously completed bookings by the user", example = "7")
        Integer completedBookings
) {}