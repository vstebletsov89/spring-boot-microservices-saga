package ru.otus.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.otus.common.enums.BookingStatus;

import java.time.Instant;

@Schema(description = "Booking response including seat assignment and booking status")
public record BookingSeatMappingResponse(

        @Schema(description = "Unique identifier of the booking", example = "627f2329-589e-436b-bb36-4474b3a5cc8e")
        String bookingId,

        @Schema(description = "Flight number associated with the booking", example = "SU1234")
        String flightNumber,

        @Schema(description = "Assigned seat number", example = "12A")
        String seatNumber,

        @Schema(description = "Timestamp when the booking was created", example = "2025-05-01T12:34:56Z")
        Instant reservedAt,

        @Schema(description = "Current status of the booking", example = "RESERVED")
        BookingStatus status) {}
