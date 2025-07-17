package ru.otus.discountservice.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Map of feature flags and their current enabled/disabled states")
@Builder
public record FeatureFlagsResponse(
        @Schema(description = "Early booking discount enabled", example = "true")
        boolean earlyBooking,

        @Schema(description = "Loyalty discount enabled", example = "true")
        boolean loyalty,

        @Schema(description = "Student discount enabled", example = "false")
        boolean student,

        @Schema(description = "Summer seasonal discount enabled", example = "true")
        boolean summerDiscount
) {}