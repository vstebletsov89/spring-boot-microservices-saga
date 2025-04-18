package ru.otus.flight.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.enums.FlightStatus;
import ru.otus.common.event.FlightCreatedEvent;
import ru.otus.common.request.CreateFlightRequest;
import ru.otus.flight.entity.Airport;
import ru.otus.flight.entity.Flight;
import ru.otus.flight.publisher.FlightPublisher;
import ru.otus.flight.repository.AirportRepository;
import ru.otus.flight.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FlightWriteService.class)
class FlightWriteServiceTest {

    @Autowired
    private FlightWriteService flightWriteService;

    @MockitoBean
    private FlightRepository flightRepository;

    @MockitoBean
    private AirportRepository airportRepository;

    @MockitoBean
    private FlightPublisher flightPublisher;

    private final Airport departureAirport = Airport.builder()
            .code("SVO")
            .city("Moscow")
            .country("Russia")
            .build();

    private final Airport arrivalAirport = Airport.builder()
            .code("JFK")
            .city("New York")
            .country("USA")
            .build();

    private final CreateFlightRequest request = new CreateFlightRequest(
            "FL123",
            "SVO",
            "JFK",
            FlightStatus.SCHEDULED,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(1).plusHours(10),
            new BigDecimal("999.99"),
            180,
            new BigDecimal("10.00")
    );

    @Test
    void shouldCreateFlightAndPublishEvent() {
        when(airportRepository.findById("SVO")).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById("JFK")).thenReturn(Optional.of(arrivalAirport));

        flightWriteService.createFlight(request);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        Flight flight = captor.getValue();

        assertEquals("FL123", flight.getFlightNumber());
        assertEquals(departureAirport, flight.getDepartureAirport());
        assertEquals(arrivalAirport, flight.getArrivalAirport());
        assertEquals(0, flight.getReservedSeats());

        verify(flightPublisher).publish(eq("FL123"), any(FlightCreatedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenDepartureAirportMissing() {
        when(airportRepository.findById("SVO")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> flightWriteService.createFlight(request));

        assertEquals("Invalid departure airport code", exception.getMessage());
        verifyNoInteractions(flightRepository, flightPublisher);
    }

    @Test
    void shouldThrowExceptionWhenArrivalAirportMissing() {
        when(airportRepository.findById("SVO")).thenReturn(Optional.of(departureAirport));
        when(airportRepository.findById("JFK")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> flightWriteService.createFlight(request));

        assertEquals("Invalid arrival airport code", exception.getMessage());
        verifyNoInteractions(flightRepository, flightPublisher);
    }
}