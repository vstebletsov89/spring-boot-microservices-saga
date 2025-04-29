package ru.otus.common.request;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record FlightSearchRequest(

        @NotBlank(message = "Departure airport code must not be blank")
        @Size(min = 3, max = 3, message = "Departure airport code must be exactly 3 characters (IATA code)")
        String fromCode,

        @NotBlank(message = "Arrival airport code must not be blank")
        @Size(min = 3, max = 3, message = "Arrival airport code must be exactly 3 characters (IATA code)")
        String toCode,

        @NotNull(message = "Departure date must not be null")
        @Future(message = "Departure date must be in the future")
        LocalDateTime departureDate,

        @Future(message = "Return date must be in the future")
        LocalDateTime returnDate,

        @Min(value = 1, message = "Passenger count must be at least 1")
        int passengerCount

) {}