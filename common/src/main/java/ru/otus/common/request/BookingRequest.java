package ru.otus.common.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookingRequest(

        @NotBlank(message = "User ID must not be blank")
        String userId,

        @NotBlank(message = "Flight number must not be blank")
        @Size(max = 10, message = "Flight number must be at most 10 characters")
        String flightNumber) {}