package ru.otus.flightquery.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.FlightResponse;
import ru.otus.common.response.RoundTripFlightResponse;
import ru.otus.flightquery.entity.Flight;
import ru.otus.flightquery.repository.FlightRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightQueryService {

    private final FlightRepository flightRepository;

    public RoundTripFlightResponse searchRoundTripFlights(FlightSearchRequest request) {
        List<Flight> outboundFlights = flightRepository.findFlightsBetweenDates(
                request.fromCode(),
                request.toCode(),
                request.departureDate(),
                request.returnDate()
        );

        List<Flight> returnFlights = flightRepository.findFlightsBetweenDates(
                request.toCode(),
                request.fromCode(),
                request.departureDate(),
                request.returnDate()
        );

        List<FlightResponse> outbound = outboundFlights.stream()
                .filter(f ->
                        f.getTotalSeats() - f.getReservedSeats() >= request.passengerCount())
                .map(f -> new FlightResponse(
                        f.getFlightNumber(),
                        f.getDepartureAirportCode(),
                        f.getArrivalAirportCode(),
                        f.getDepartureTime(),
                        f.getArrivalTime(),
                        f.getPrice()))
                .toList();

        List<FlightResponse> back = returnFlights.stream()
                .filter(f ->
                        f.getTotalSeats() - f.getReservedSeats() >= request.passengerCount())
                .map(f -> new FlightResponse(
                        f.getFlightNumber(),
                        f.getDepartureAirportCode(),
                        f.getArrivalAirportCode(),
                        f.getDepartureTime(),
                        f.getArrivalTime(),
                        f.getPrice()))
                .toList();

        return new RoundTripFlightResponse(outbound, back);
    }
}
