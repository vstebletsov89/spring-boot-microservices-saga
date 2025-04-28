package ru.otus.flight.service;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.common.command.ReleaseSeatCommand;
import ru.otus.common.command.ReserveSeatCommand;
import ru.otus.common.enums.BookingStatus;
import ru.otus.common.enums.FlightStatus;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.common.entity.Flight;
import ru.otus.common.kafka.BookingSeatCreatedEvent;
import ru.otus.common.kafka.BookingSeatUpdatedEvent;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.common.saga.SeatReservationFailedEvent;
import ru.otus.common.saga.SeatReservedEvent;
import ru.otus.flight.publisher.BookingPublisher;
import ru.otus.flight.publisher.FlightPublisher;
import ru.otus.flight.repository.BookingSeatMappingRepository;
import ru.otus.flight.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SeatService.class)
public class SeatServiceTest {

    private static final String FLIGHT_NUMBER = "FL123";
    private static final String BOOKING_ID = "B123";
    private static final String USER_ID = "U1";

    @MockitoBean
    private FlightRepository flightRepository;

    @MockitoBean
    private BookingSeatMappingRepository mappingRepository;

    @MockitoBean
    private EventGateway eventGateway;

    @MockitoBean
    private FlightPublisher flightPublisher;

    @MockitoBean
    private BookingPublisher bookingPublisher;

    @Autowired
    private SeatService seatService;

    @Test
    void shouldReserveSeatSuccessfully() {
        Flight flight = createFlight(0);
        ReserveSeatCommand cmd = new ReserveSeatCommand(BOOKING_ID, FLIGHT_NUMBER, USER_ID);

        when(flightRepository.findById(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));
        when(mappingRepository.findAllByFlightNumber(FLIGHT_NUMBER)).thenReturn(Collections.emptyList());

        seatService.handle(cmd);

        verify(mappingRepository).save(any(BookingSeatMapping.class));
        verify(flightRepository).save(flight);
        verify(eventGateway).publish(any(SeatReservedEvent.class));
        verify(flightPublisher).publish(eq(FLIGHT_NUMBER), any(FlightUpdatedEvent.class));
        verify(bookingPublisher).publish(eq(BOOKING_ID), any(BookingSeatCreatedEvent.class));
    }

    @Test
    void shouldReserveMultipleSeatsInOrder() {
        Flight flight = createFlight(0);
        when(flightRepository.findById(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));

        List<String> expectedSeatNumbers = List.of(
                "1A", "1B", "1C", "1D", "1E", "1F", "2A", "2B", "2C", "2D"
        );
        List<BookingSeatMapping> savedMappings = new ArrayList<>();

        when(mappingRepository.findAllByFlightNumber(FLIGHT_NUMBER)).thenAnswer(invocation -> new ArrayList<>(savedMappings));

        doAnswer(invocation -> {
            BookingSeatMapping mapping = invocation.getArgument(0);
            savedMappings.add(mapping);
            return null;
        }).when(mappingRepository).save(any(BookingSeatMapping.class));

        for (int i = 0; i < 10; i++) {
            ReserveSeatCommand cmd = new ReserveSeatCommand(
                    BOOKING_ID + i,
                    FLIGHT_NUMBER,
                    USER_ID + i
            );
            seatService.handle(cmd);
        }

        assertEquals(10, savedMappings.size());

        List<String> actualSeatNumbers = savedMappings.stream()
                .map(BookingSeatMapping::getSeatNumber)
                .collect(Collectors.toList());

        assertEquals(expectedSeatNumbers, actualSeatNumbers, "Места должны быть в порядке возрастания");

        verify(flightRepository, times(10)).save(flight);
        verify(mappingRepository, times(10)).save(any(BookingSeatMapping.class));
        verify(eventGateway, times(10)).publish(any(SeatReservedEvent.class));
        verify(flightPublisher, times(10)).publish(eq(FLIGHT_NUMBER), any(FlightUpdatedEvent.class));
        verify(bookingPublisher, times(10)).publish(anyString(), any(BookingSeatCreatedEvent.class));
    }

    @Test
    void shouldFailReservationWhenNoSeats() {
        Flight flight = createFlight(200);
        ReserveSeatCommand cmd = new ReserveSeatCommand(BOOKING_ID, FLIGHT_NUMBER, USER_ID);

        when(flightRepository.findById(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));

        seatService.handle(cmd);

        verify(eventGateway).publish(any(SeatReservationFailedEvent.class));
    }

    @Test
    void shouldReleaseSeatSuccessfully() {
        BookingSeatMapping mapping = BookingSeatMapping.builder()
                .bookingId(BOOKING_ID)
                .flightNumber(FLIGHT_NUMBER)
                .seatNumber("A1")
                .status(BookingStatus.RESERVED)
                .build();

        Flight flight = createFlight(50);

        when(mappingRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(mapping));
        when(flightRepository.findById(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));

        seatService.handle(new ReleaseSeatCommand(BOOKING_ID));

        verify(flightRepository).save(any());
        verify(mappingRepository).save(any());
        verify(flightPublisher).publish(eq(FLIGHT_NUMBER), any(FlightUpdatedEvent.class));
        verify(bookingPublisher).publish(eq(BOOKING_ID), any(BookingSeatUpdatedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenNoAvailableSeats() {
        Flight flight = createFlight(0);

        when(flightRepository.findById(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));
        when(mappingRepository.findAllByFlightNumber(FLIGHT_NUMBER))
                .thenReturn(generateAllReservedSeats(30, new char[]{'A', 'B', 'C', 'D', 'E', 'F'}));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                seatService.handle(new ReserveSeatCommand(BOOKING_ID, FLIGHT_NUMBER, USER_ID))
        );

        assertEquals("No available seats", exception.getMessage());
    }

    private Flight createFlight(int reservedSeats) {
        return Flight.builder()
                .flightNumber(FLIGHT_NUMBER)
                .status(FlightStatus.SCHEDULED)
                .departureTime(LocalDateTime.now())
                .arrivalTime(LocalDateTime.now().plusHours(2))
                .price(BigDecimal.valueOf(100))
                .totalSeats(180)
                .reservedSeats(reservedSeats)
                .overbookingPercentage(BigDecimal.valueOf(10))
                .build();
    }

    private  List<BookingSeatMapping> generateAllReservedSeats(int rows, char[] letters) {
        List<BookingSeatMapping> list = new java.util.ArrayList<>();
        for (int i = 1; i <= rows; i++) {
            for (char c : letters) {
                list.add(BookingSeatMapping.builder()
                        .seatNumber(i + String.valueOf(c))
                        .build());
            }
        }
        return list;
    }
}