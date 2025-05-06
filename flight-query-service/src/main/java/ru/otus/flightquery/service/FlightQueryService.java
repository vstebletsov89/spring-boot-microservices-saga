package ru.otus.flightquery.service;


import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.FlightResponse;
import ru.otus.common.response.RoundTripFlightResponse;
import ru.otus.flightquery.mapper.FlightMapper;
import ru.otus.flightquery.repository.FlightRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightQueryService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Cacheable(value = "roundTripFlights", key = "#request.hashCode()")
    public RoundTripFlightResponse searchRoundTripFlights(FlightSearchRequest request) {
        List<FlightResponse> outbound = flightRepository.findFlightsBetweenDates(
                        request.fromCode(),
                        request.toCode(),
                        request.departureDate(),
                        request.returnDate()
                ).stream()
                .filter(f -> f.getTotalSeats() - f.getReservedSeats() >= request.passengerCount())
                .map(flightMapper::toResponse)
                .toList();

        List<FlightResponse> back = flightRepository.findFlightsBetweenDates(
                        request.toCode(),
                        request.fromCode(),
                        request.departureDate(),
                        request.returnDate()
                ).stream()
                .filter(f -> f.getTotalSeats() - f.getReservedSeats() >= request.passengerCount())
                .map(flightMapper::toResponse)
                .toList();

        return new RoundTripFlightResponse(outbound, back);
    }
}