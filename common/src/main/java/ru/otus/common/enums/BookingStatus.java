package ru.otus.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a booking")
public enum BookingStatus {

    @Schema(description = "Booking has been reserved but not yet paid")
    RESERVED,

    @Schema(description = "Booking has been paid and confirmed")
    PAID,

    @Schema(description = "Booking was cancelled")
    CANCELLED
}
