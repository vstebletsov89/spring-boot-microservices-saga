package ru.otus.flightquery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.common.event.FlightCreatedEvent;
import ru.otus.common.event.FlightUpdatedEvent;
import ru.otus.flightquery.entity.Flight;
import ru.otus.flightquery.repository.FlightRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightSyncService {

    private final FlightRepository flightRepository;

    @Transactional
    public void handleCreated(FlightCreatedEvent event) {
        log.info("Syncing new flight from event: {}", event);

        Flight flight = Flight.builder()
                .flightNumber(event.flightNumber())
                .departureAirportCode(event.departureAirportCode())
                .arrivalAirportCode(event.arrivalAirportCode())
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
