package ru.otus.discountservice.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Schema(description = "Response indicating result of feature reload operation")
@Builder
public record FeatureReloadResponse(
        @Schema(description = "Status of the reload action", example = "reloaded")
        String status,

        @Schema(description = "Timestamp when reload occurred", example = "2025-09-21T12:34:56.789Z")
        Instant occurredAt
) {}
