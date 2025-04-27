package ru.otus.flight.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.event.FlightCreatedEvent;
import ru.otus.common.request.CreateFlightRequest;
import ru.otus.common.entity.Airport;
import ru.otus.common.entity.Flight;
import ru.otus.flight.publisher.FlightPublisher;
import ru.otus.flight.repository.AirportRepository;
import ru.otus.flight.repository.FlightRepository;

@Service
@RequiredArgsConstructor
public class FlightWriteService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final FlightPublisher flightPublisher;

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
        publishFlightCreatedEvent(flight);
    }

    private void publishFlightCreatedEvent(Flight flight) {

        FlightCreatedEvent event = new FlightCreatedEvent(
                flight.getFlightNumber(),
                flight.getDepartureAirport().getCode(),
                flight.getArrivalAirport().getCode(),
                flight.getStatus(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice(),
                flight.getTotalSeats(),
                flight.getReservedSeats(),
                flight.getOverbookingPercentage()
        );
        flightPublisher.publish(flight.getFlightNumber(), event);
    }
}
