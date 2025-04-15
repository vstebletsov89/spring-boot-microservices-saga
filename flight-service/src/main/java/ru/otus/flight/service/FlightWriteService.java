package ru.otus.flight.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.request.CreateFlightRequest;
import ru.otus.flight.entity.Airport;
import ru.otus.flight.entity.Flight;
import ru.otus.flight.repository.AirportRepository;
import ru.otus.flight.repository.FlightRepository;

@Service
@RequiredArgsConstructor
public class FlightWriteService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    @Transactional
    public void createFlight(CreateFlightRequest request) {
        Airport departureAirport = airportRepository.findById(request.departureAirportCode())
                .orElseThrow(() -> new RuntimeException("Invalid departure airport code"));

        Airport arrivalAirport = airportRepository.findById(request.arrivalAirportCode())
                .orElseThrow(() -> new RuntimeException("Invalid arrival airport code"));

        Flight flight = new Flight(
                request.flightNumber(),
                departureAirport,
                arrivalAirport,
                request.status(),
                request.departureTime(),
                request.arrivalTime(),
                request.price(),
                request.totalSeats(),
                0,
                request.overbookingPercentage());

        flightRepository.save(flight);
    }
}
