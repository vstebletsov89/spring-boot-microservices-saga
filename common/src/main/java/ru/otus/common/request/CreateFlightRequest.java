package ru.otus.common.request;

import jakarta.validation.constraints.*;
import ru.otus.common.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateFlightRequest(

        @NotBlank(message = "Flight number must not be blank")
        @Size(max = 10, message = "Flight number must be at most 10 characters")
        String flightNumber,

        @NotBlank(message = "Departure airport code must not be blank")
        @Size(min = 3, max = 3, message = "Departure airport code must be exactly 3 characters (IATA code)")
        String departureAirportCode,

        @NotBlank(message = "Arrival airport code must not be blank")
        @Size(min = 3, max = 3, message = "Arrival airport code must be exactly 3 characters (IATA code)")
        String arrivalAirportCode,

        @NotNull(message = "Flight status must not be null")
        FlightStatus status,

        @NotNull(message = "Departure time must not be null")
        @Future(message = "Departure time must be in the future")
        LocalDateTime departureTime,

        @NotNull(message = "Arrival time must not be null")
        @Future(message = "Arrival time must be in the future")
        LocalDateTime arrivalTime,

        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @Min(value = 1, message = "Total seats must be at least 1")
        int totalSeats,

        @NotNull(message = "Overbooking percentage must not be null")
        @DecimalMin(value = "0.0", message = "Overbooking percentage must not be negative")
        BigDecimal overbookingPercentage

) {}
