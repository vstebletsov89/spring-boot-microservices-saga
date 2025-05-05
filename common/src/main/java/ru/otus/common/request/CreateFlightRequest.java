package ru.otus.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.otus.common.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Flight creation request data")
public record CreateFlightRequest(

        @NotBlank(message = "Flight number must not be blank")
        @Size(max = 10, message = "Flight number must be at most 10 characters")
        @Schema(description = "Flight number", example = "SU1234")
        String flightNumber,

        @NotBlank(message = "Departure airport code must not be blank")
        @Size(min = 3, max = 3, message = "Departure airport code must be exactly 3 characters (IATA code)")
        @Schema(description = "Departure airport IATA code", example = "SVO")
        String departureAirportCode,

        @NotBlank(message = "Arrival airport code must not be blank")
        @Size(min = 3, max = 3, message = "Arrival airport code must be exactly 3 characters (IATA code)")
        @Schema(description = "Arrival airport IATA code", example = "JFK")
        String arrivalAirportCode,

        @NotNull(message = "Flight status must not be null")
        @Schema(description = "Current status of the flight", example = "SCHEDULED")
        FlightStatus status,

        @NotNull(message = "Departure time must not be null")
        @Future(message = "Departure time must be in the future")
        @Schema(description = "Scheduled departure time (must be in the future)", example = "2025-06-01T14:30:00")
        LocalDateTime departureTime,

        @NotNull(message = "Arrival time must not be null")
        @Future(message = "Arrival time must be in the future")
        @Schema(description = "Scheduled arrival time (must be in the future)", example = "2025-06-01T20:45:00")
        LocalDateTime arrivalTime,

        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Schema(description = "Flight ticket price", example = "299.99")
        BigDecimal price,

        @Min(value = 1, message = "Total seats must be at least 1")
        @Schema(description = "Total number of seats available on the flight", example = "180")
        int totalSeats,

        @NotNull(message = "Overbooking percentage must not be null")
        @DecimalMin(value = "0.0", message = "Overbooking percentage must not be negative")
        @Schema(description = "Allowed overbooking percentage", example = "5.0")
        BigDecimal overbookingPercentage) {}
