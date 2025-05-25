package ru.otus.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cancel reservation request data")
public record CancelReservationRequest(

        @NotBlank(message = "Booking ID must not be blank")
        @Size(max = 36, message = "Booking ID must be at most 36 characters")
        @Schema(description = "Booking ID", example = "9a7759be-8b7a-4e27-a10a-52a0e2b7a7ce")
        String bookingId,

        @NotBlank(message = "User ID must not be blank")
        @Schema(description = "User ID", example = "627f2329-589e-436b-bb36-4474b3a5cc8e")
        String userId,

        @NotBlank(message = "Flight number must not be blank")
        @Size(max = 10, message = "Flight number must be at most 10 characters")
        @Schema(description = "Flight number", example = "SU1234")
        String flightNumber
) {}
