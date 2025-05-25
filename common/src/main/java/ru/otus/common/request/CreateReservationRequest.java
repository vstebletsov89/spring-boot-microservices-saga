package ru.otus.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Flight booking request data")
public record CreateReservationRequest(

        @NotBlank(message = "User ID must not be blank")
        @Schema(description = "User ID", example = "627f2329-589e-436b-bb36-4474b3a5cc8e")
        String userId,

        @NotBlank(message = "Flight number must not be blank")
        @Size(max = 10, message = "Flight number must be at most 10 characters")
        @Schema(description = "Flight number", example = "SU1234")
        String flightNumber,

        @Schema(description = "Optional seat number selected by the user", example = "12A")
        @Size(max = 5, message = "Seat number must be at most 5 characters")
        String seatNumber
) {}