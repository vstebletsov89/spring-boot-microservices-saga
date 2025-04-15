package ru.otus.flightquery.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.FlightStatusResponse;
import ru.otus.flightquery.repository.FlightRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightQueryService {

    private final FlightRepository flightRepository;

    public List<FlightStatusResponse> searchFlights(FlightSearchRequest request) {
        return flightRepository.searchFlights(
                        request.fromCity(),
                        request.toCity(),
                        request.departureDate(),
                        request.returnDate())
                .stream()
                .filter(flight -> flight.getTotalSeats() - flight.getReservedSeats() >= request.passengers())
                .map(f -> new FlightStatusResponse(
                        f.getFlightNumber(),
                        f.getStatus().name(),
                        f.getDepartureTime(),
                        f.getArrivalTime()
                ))
                .toList();
    }
}
