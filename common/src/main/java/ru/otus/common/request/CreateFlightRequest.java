package ru.otus.common.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.otus.common.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateFlightRequest(

        @NotBlank
        String flightNumber,

        @NotBlank
        String departureAirportCode,

        @NotBlank
        String arrivalAirportCode,

        @NotNull
        FlightStatus status,

        @NotNull
        LocalDateTime departureTime,

        @NotNull
        LocalDateTime arrivalTime,

        @NotNull @DecimalMin("0.0")
        BigDecimal price,

        @Min(1)
        int totalSeats,

        @NotNull @DecimalMin("0.0")
        BigDecimal overbookingPercentage) {}
