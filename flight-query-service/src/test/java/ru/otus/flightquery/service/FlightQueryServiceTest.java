package ru.otus.flightquery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.entity.Airport;
import ru.otus.common.entity.Flight;
import ru.otus.common.request.FlightSearchRequest;
import ru.otus.common.response.RoundTripFlightResponse;
import ru.otus.flightquery.mapper.FlightMapperImpl;
import ru.otus.flightquery.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {FlightQueryService.class, FlightMapperImpl.class})
class FlightQueryServiceTest {

    @MockitoBean
    private FlightRepository flightRepository;

    @Autowired
    private FlightQueryService flightQueryService;

    @Test
    void shouldReturnAvailableFlightsForRoundTrip() {
        Flight outbound = createFlight("FL123", "SVO", "JFK", 180, 100);
        Flight inbound = createFlight("FL321", "JFK", "SVO", 180, 50);
        var request = new FlightSearchRequest("SVO", "JFK", LocalDateTime.now(), LocalDateTime.now().plusDays(7), 2);

        when(flightRepository.findFlightsBetweenDates("SVO", "JFK", request.departureDate(), request.returnDate()))
                .thenReturn(List.of(outbound));
        when(flightRepository.findFlightsBetweenDates("JFK", "SVO", request.departureDate(), request.returnDate()))
                .thenReturn(List.of(inbound));

        RoundTripFlightResponse result = flightQueryService.searchRoundTripFlights(request);

        assertThat(result.outboundFlights()).hasSize(1);
        assertThat(result.returnFlights()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyIfNotEnoughSeats() {
        Flight outbound = createFlight("FL999", "SVO", "JFK", 180, 179);
        var request = new FlightSearchRequest("SVO", "JFK", LocalDateTime.now(), LocalDateTime.now().plusDays(7), 2);

        when(flightRepository.findFlightsBetweenDates("SVO", "JFK", request.departureDate(), request.returnDate()))
                .thenReturn(List.of(outbound));
        when(flightRepository.findFlightsBetweenDates("JFK", "SVO", request.departureDate(), request.returnDate()))
                .thenReturn(List.of());

        RoundTripFlightResponse result = flightQueryService.searchRoundTripFlights(request);

        assertThat(result.outboundFlights()).isEmpty();
        assertThat(result.returnFlights()).isEmpty();
    }

    private Flight createFlight(String number, String from, String to, int totalSeats, int reservedSeats) {
        return Flight.builder()
                .flightNumber(number)
                .departureAirport(new Airport("DXB", "Dubai airport", "Dubai", "UAE"))
                .arrivalAirport(new Airport("SVO", "Sheremetyevo airport", "Moscow", "Russia"))
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(8))
                .price(new BigDecimal("500.00"))
                .totalSeats(totalSeats)
                .reservedSeats(reservedSeats)
                .build();
    }
}