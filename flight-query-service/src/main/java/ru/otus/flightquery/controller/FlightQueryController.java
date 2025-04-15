package ru.otus.flightquery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.FlightStatusResponse;
import ru.otus.flightquery.service.FlightQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightQueryController {

    private final FlightQueryService flightQueryService;

    @PostMapping("/search")
    public ResponseEntity<List<FlightStatusResponse>> search(@RequestBody @Valid FlightSearchRequest request) {
        List<FlightStatusResponse> flights = flightQueryService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }
}
