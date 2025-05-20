package ru.otus.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Flight response with detailed flight and pricing information")
public record FlightResponse(

        @Schema(description = "Flight number", example = "SU1234")
        String flightNumber,

        @Schema(description = "IATA code of the departure airport", example = "SVO")
        String departureAirportCode,

        @Schema(description = "IATA code of the arrival airport", example = "JFK")
        String arrivalAirportCode,

        @Schema(description = "Flight departure time (local)", example = "2025-06-10T10:30:00")
        LocalDateTime departureTime,

        @Schema(description = "Flight arrival time (local)", example = "2025-06-10T16:20:00")
        LocalDateTime arrivalTime,

        @Schema(description = "Ticket price", example = "499.99")
        BigDecimal price
) {}
