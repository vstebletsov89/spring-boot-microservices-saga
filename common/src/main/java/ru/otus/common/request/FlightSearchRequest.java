package ru.otus.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(description = "Flight search request data for round-trip flights")
public record FlightSearchRequest(

        @NotBlank(message = "Departure airport code must not be blank")
        @Size(min = 3, max = 3, message = "Departure airport code must be exactly 3 characters (IATA code)")
        @Schema(description = "IATA code of the departure airport", example = "SVO")
        String fromCode,

        @NotBlank(message = "Arrival airport code must not be blank")
        @Size(min = 3, max = 3, message = "Arrival airport code must be exactly 3 characters (IATA code)")
        @Schema(description = "IATA code of the arrival airport", example = "JFK")
        String toCode,

        @NotNull(message = "Departure date must not be null")
        @Future(message = "Departure date must be in the future")
        @Schema(description = "Outbound departure date and time", example = "2025-07-01T10:00:00")
        LocalDateTime departureDate,

        @Future(message = "Return date must be in the future")
        @Schema(description = "Return date and time (optional for one-way search)", example = "2025-07-15T18:00:00")
        LocalDateTime returnDate,

        @Min(value = 1, message = "Passenger count must be at least 1")
        @Schema(description = "Number of passengers", example = "2")
        int passengerCount) {}