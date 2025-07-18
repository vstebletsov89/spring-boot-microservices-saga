package ru.otus.discountservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.request.DiscountRequest;
import ru.otus.common.response.DiscountResponse;
import ru.otus.discountservice.service.DiscountService;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discount Calculation", description = "Endpoint for calculating final price with applied discounts")
public class DiscountController {

    private final DiscountService discountService;

    @Operation(
            summary = "Calculate final price after discounts",
            description = "Returns the final price after applying all applicable discount rules"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Discount calculated successfully",
                    content = @Content(schema = @Schema(implementation = DiscountResponse.class))
            )
    })
    @PostMapping("/calculate")
    public DiscountResponse calculateFinalPrice(
            @RequestBody @Valid DiscountRequest request) {

        log.info("Calculating discount for request: {}", request);
        return discountService.calculateFinalPrice(request);
    }
}
