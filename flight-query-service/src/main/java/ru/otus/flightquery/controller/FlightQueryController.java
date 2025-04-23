package ru.otus.flightquery.controller;

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
public class FlightQueryController {

    private final FlightQueryService flightQueryService;


    @PostMapping("/search")
    public RoundTripFlightResponse searchFlights(@RequestBody FlightSearchRequest request) {
        log.info("Search flights request: {}", request);
        return flightQueryService.searchRoundTripFlights(request);
    }
}
