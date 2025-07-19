package ru.otus.discountservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.discountservice.feature.FeatureChecker;
import ru.otus.discountservice.response.FeatureFlagsResponse;
import ru.otus.discountservice.response.FeatureReloadResponse;

import java.time.Instant;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Features", description = "Endpoints for managing discount feature flags")
public class FeatureController {

    private final FeatureChecker featureChecker;

    @Operation(
            summary = "Get current feature flag states",
            description = "Returns the current enabled/disabled status of all discount feature flags"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feature flags retrieved successfully")
    })
    @GetMapping
    public FeatureFlagsResponse getFeatureFlags() {
        log.debug("Fetching current feature flags");

        return FeatureFlagsResponse.builder()
                .earlyBooking(featureChecker.isEarlyBookingEnabled())
                .loyalty(featureChecker.isLoyaltyEnabled())
                .student(featureChecker.isStudentEnabled())
                .summerDiscount(featureChecker.isSummerDiscountEnabled())
                .build();
    }

}
