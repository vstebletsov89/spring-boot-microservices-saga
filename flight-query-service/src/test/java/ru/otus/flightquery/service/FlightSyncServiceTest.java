package ru.otus.flightquery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.enums.FlightStatus;
import ru.otus.common.event.FlightCreatedEvent;
import ru.otus.common.event.FlightUpdatedEvent;
import ru.otus.flightquery.entity.Flight;
import ru.otus.flightquery.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FlightSyncService.class)
class FlightSyncServiceTest {


    @MockitoBean
    private FlightRepository flightRepository;

    @Autowired
    private FlightSyncService flightSyncService;


    private final LocalDateTime departureTime = LocalDateTime.now().plusDays(1);
    private final LocalDateTime arrivalTime = departureTime.plusHours(8);


    @Test
    void shouldHandleFlightCreatedEvent() {
        FlightCreatedEvent event = new FlightCreatedEvent(
                "FL123", "SVO", "JFK",
                FlightStatus.SCHEDULED,
                departureTime, arrivalTime, new BigDecimal("999.99"),
                180, 0, new BigDecimal("10.00")
        );

        flightSyncService.handleCreated(event);

        verify(flightRepository).save(argThat(f ->
                f.getFlightNumber().equals(event.flightNumber()) &&
                f.getDepartureAirportCode().equals(event.departureAirportCode()) &&
                f.getArrivalAirportCode().equals(event.arrivalAirportCode()) &&
                f.getStatus() == event.status() &&
                f.getDepartureTime().equals(departureTime) &&
                f.getArrivalTime().equals(arrivalTime) &&
                f.getPrice().compareTo(event.price()) == 0 &&
                f.getTotalSeats() == event.totalSeats() &&
                f.getReservedSeats() == event.reservedSeats() &&
                f.getOverbookingPercentage().compareTo(event.overbookingPercentage()) == 0
        ));
    }

    @Test
    void shouldHandleFlightUpdatedEvent() {
        Flight existingFlight = new Flight();
        existingFlight.setFlightNumber("FL123");

        when(flightRepository.findById("FL123")).thenReturn(Optional.of(existingFlight));

        FlightUpdatedEvent event = new FlightUpdatedEvent(
                "FL123", FlightStatus.CANCELLED,
                departureTime, arrivalTime, new BigDecimal("888.88"),
                150, 10, new BigDecimal("5.00")
        );

        flightSyncService.handleUpdated(event);

        verify(flightRepository).save(argThat(f ->
                f.getFlightNumber().equals(event.flightNumber()) &&
                f.getStatus() == event.status() &&
                f.getDepartureTime().equals(departureTime) &&
                f.getArrivalTime().equals(arrivalTime) &&
                f.getPrice().compareTo(event.price()) == 0 &&
                f.getTotalSeats() == event.totalSeats() &&
                f.getReservedSeats() == event.reservedSeats() &&
                f.getOverbookingPercentage().compareTo(event.overbookingPercentage()) == 0
        ));
    }

    @Test
    void shouldThrowWhenFlightNotFoundForUpdate() {
        when(flightRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        FlightUpdatedEvent event = new FlightUpdatedEvent(
                "UNKNOWN", FlightStatus.SCHEDULED,
                departureTime, arrivalTime, new BigDecimal("100.00"),
                100, 10, new BigDecimal("5.00")
        );

       assertThatThrownBy(() ->
                        flightSyncService
                                .handleUpdated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Flight not found");
    }
}