package ru.otus.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current status of the flight")
public enum FlightStatus {

    @Schema(description = "Flight is scheduled and not yet boarding")
    SCHEDULED,

    @Schema(description = "Passengers are boarding the flight")
    BOARDING,

    @Schema(description = "Flight has departed")
    DEPARTED,

    @Schema(description = "Flight has landed at the destination")
    LANDED,

    @Schema(description = "Flight has been cancelled")
    CANCELLED,

    @Schema(description = "Flight has been delayed")
    DELAYED
}
