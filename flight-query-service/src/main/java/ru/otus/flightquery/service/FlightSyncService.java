package ru.otus.flightquery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.entity.Airport;
import ru.otus.common.entity.Flight;
import ru.otus.common.kafka.FlightCreatedEvent;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.flightquery.repository.AirportRepository;
import ru.otus.flightquery.repository.FlightRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightSyncService {

    private final FlightRepository flightRepository;

    private final AirportRepository airportRepository;

    @Transactional
    public void handleCreated(FlightCreatedEvent event) {
        log.info("Syncing new flight from event: {}", event);

        Airport departureAirport = airportRepository.findById(event.departureAirportCode())
                .orElseThrow(() -> new RuntimeException("Invalid departure airport code"));

        Airport arrivalAirport = airportRepository.findById(event.arrivalAirportCode())
                .orElseThrow(() -> new RuntimeException("Invalid arrival airport code"));

        Flight flight = Flight.builder()
                .flightNumber(event.flightNumber())
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .status(event.status())
                .departureTime(event.departureTime())
                .arrivalTime(event.arrivalTime())
                .price(event.price())
                .totalSeats(event.totalSeats())
                .reservedSeats(event.reservedSeats())
                .overbookingPercentage(event.overbookingPercentage())
                .build();

        flightRepository.save(flight);
    }

    @Transactional
    public void handleUpdated(FlightUpdatedEvent event) {
        log.info("Updating flight from event: {}", event);

        Flight flight = flightRepository.findById(event.flightNumber())
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        flight.setStatus(event.status());
        flight.setDepartureTime(event.departureTime());
        flight.setArrivalTime(event.arrivalTime());
        flight.setPrice(event.price());
        flight.setTotalSeats(event.totalSeats());
        flight.setReservedSeats(event.reservedSeats());
        flight.setOverbookingPercentage(event.overbookingPercentage());

        flightRepository.save(flight);
    }
}
