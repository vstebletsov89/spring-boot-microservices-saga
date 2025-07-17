package ru.otus.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response containing the final price after discounts")
public record DiscountResponse(

        @Schema(description = "Final calculated price after applying all eligible discounts", example = "374.99")
        BigDecimal finalPrice
) {}
