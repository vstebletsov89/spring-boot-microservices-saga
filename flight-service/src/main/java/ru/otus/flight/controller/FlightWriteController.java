package ru.otus.flight.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.otus.common.request.CreateFlightRequest;
import ru.otus.flight.service.FlightWriteService;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightWriteController {

    private final FlightWriteService flightWriteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createFlight(@RequestBody @Valid CreateFlightRequest request) {
        flightWriteService.createFlight(request);
    }
}
