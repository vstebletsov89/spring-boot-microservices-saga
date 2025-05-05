package ru.otus.flight.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.otus.common.request.CreateFlightRequest;
import ru.otus.flight.service.FlightWriteService;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Management", description = "Endpoints for creating and managing flights")
public class FlightWriteController {

    private final FlightWriteService flightWriteService;

    @Operation(
            summary = "Create a new flight",
            description = "Registers a new flight with the given details such as departure, arrival, times, price, and overbooking strategy."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flight successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid flight data provided")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createFlight(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Flight creation data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateFlightRequest.class))
            )
            @RequestBody @Valid CreateFlightRequest request) {
        flightWriteService.createFlight(request);
    }
}
