package ru.otus.flight.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.kafka.FlightCreatedEvent;
import ru.otus.common.request.CreateFlightRequest;
import ru.otus.common.entity.Airport;
import ru.otus.common.entity.Flight;
import ru.otus.flight.publisher.FlightPublisher;
import ru.otus.flight.repository.AirportRepository;
import ru.otus.flight.repository.FlightRepository;

import java.util.UUID;

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

        var flight = Flight.builder()
                .flightNumber(request.flightNumber())
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .status(request.status())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .price(request.price())
                .totalSeats(request.totalSeats())
                .reservedSeats(0)
                .overbookingPercentage(request.overbookingPercentage())
                .build();

        flightRepository.save(flight);
        publishFlightCreatedEvent(flight);
    }

    private void publishFlightCreatedEvent(Flight flight) {

        FlightCreatedEvent event = new FlightCreatedEvent(
                UUID.randomUUID().toString(),
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
