package ru.otus.flightquery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.RoundTripFlightResponse;
import ru.otus.flightquery.service.FlightQueryService;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flight Search", description = "Endpoints for searching available round-trip flights")
public class FlightQueryController {

    private final FlightQueryService flightQueryService;


    @Operation(
            summary = "Search for round-trip flights",
            description = "Returns available round-trip flights based on search criteria including route, dates, and passenger count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flights found and returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PostMapping("/search")
    public RoundTripFlightResponse searchFlights(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Flight search request data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FlightSearchRequest.class))
            )
            @RequestBody @Valid FlightSearchRequest request) {
        log.info("Search flights request: {}", request);
        return flightQueryService.searchRoundTripFlights(request);
    }
}
