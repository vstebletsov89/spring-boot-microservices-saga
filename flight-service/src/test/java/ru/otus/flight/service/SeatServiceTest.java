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
import ru.otus.flight.entity.BookingSeatMapping;
import ru.otus.common.entity.Flight;
import ru.otus.common.kafka.FlightUpdatedEvent;
import ru.otus.common.saga.SeatReservationFailedEvent;
import ru.otus.common.saga.SeatReservedEvent;
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


import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private SeatService seatService;

    @Test
    void shouldIgnoreCancelledBookings() {
        Flight flight = createFlight(0);
        when(flightRepository.findByFlightNumberForUpdate(FLIGHT_NUMBER))
                .thenReturn(Optional.of(flight));

        List<BookingSeatMapping> existingBookings = List.of(
                BookingSeatMapping.builder()
                        .seatNumber("1A")
                        .bookingId("b1")
                        .flightNumber(FLIGHT_NUMBER)
                        .build(),
                BookingSeatMapping.builder()
                        .seatNumber("1B")
                        .bookingId("A1")
                        .flightNumber(FLIGHT_NUMBER)
                        .build()
        );

        List<BookingSeatMapping> savedBookings = new ArrayList<>();
        when(mappingRepository.findAllByFlightNumberForUpdate(FLIGHT_NUMBER))
                .thenReturn(existingBookings);

        doAnswer(inv -> {
            BookingSeatMapping mapping = inv.getArgument(0);
            savedBookings.add(mapping);
            return mapping;
        }).when(mappingRepository).save(any(BookingSeatMapping.class));

        seatService.handle(new ReserveSeatCommand(BOOKING_ID, FLIGHT_NUMBER, USER_ID));

        List<String> alreadyUsed = existingBookings.stream()
                .map(BookingSeatMapping::getSeatNumber)
                .toList();

        List<String> allSeats = new ArrayList<>(alreadyUsed);
        allSeats.add(savedBookings.get(0).getSeatNumber());

        assertThat(allSeats).doesNotHaveDuplicates();
    }

    @Test
    void shouldReserveAllOverbookedSeats() {
        // 220 seats must be reserved
        final String flightNumber = "FL200";
        Flight flight = Flight.builder()
                .flightNumber(flightNumber)
                .status(FlightStatus.SCHEDULED)
                .departureTime(LocalDateTime.now())
                .arrivalTime(LocalDateTime.now().plusHours(2))
                .price(BigDecimal.valueOf(100))
                .totalSeats(200)
                .reservedSeats(0)
                .overbookingPercentage(BigDecimal.valueOf(10))
                .build();


        when(flightRepository.findByFlightNumberForUpdate(flightNumber))
                .thenReturn(Optional.of(flight));

        List<BookingSeatMapping> saved = new ArrayList<>();
        when(mappingRepository.findAllByFlightNumberForUpdate(flightNumber))
                .thenAnswer(invocation -> new ArrayList<>(saved));

        doAnswer(inv -> {
            BookingSeatMapping m = inv.getArgument(0);
            saved.add(m);
            return m;
        }).when(mappingRepository).save(any(BookingSeatMapping.class));

        for (int i = 0; i < 220; i++) {
            String bookingId = "B" + i;
            String userId    = "U" + i;
            seatService.handle(new ReserveSeatCommand(bookingId, flightNumber, userId));
        }

        assertEquals(220, saved.size());
        verify(mappingRepository, times(220)).save(any(BookingSeatMapping.class));
        verify(flightRepository, times(220)).save(flight);
        verify(eventGateway,    times(220)).publish(any(SeatReservedEvent.class));
        verify(flightPublisher, times(220)).publish(eq(flightNumber), any(FlightUpdatedEvent.class));
    }


    @Test
    void shouldCorrectlyCalculateFreeSeats() {
        Flight flight = new Flight();
        flight.setTotalSeats(100);
        flight.setReservedSeats(0);
        flight.setOverbookingPercentage(BigDecimal.valueOf(5));

        BigDecimal freeSeats = seatService.calculateFreeSeats(flight);

        // 100 * 1.05 = 105
        assertThat(freeSeats).isEqualByComparingTo(BigDecimal.valueOf(105));
    }

    @Test
    void shouldCorrectlyCalculateFreeSeats2() {
        Flight flight = new Flight();
        flight.setTotalSeats(100);
        flight.setReservedSeats(80);
        flight.setOverbookingPercentage(BigDecimal.valueOf(5));

        BigDecimal freeSeats = seatService.calculateFreeSeats(flight);

        // 100 * 1.05 = 105 - 80 = 25
        assertThat(freeSeats).isEqualByComparingTo(BigDecimal.valueOf(25));
    }

    @Test
    void shouldReserveSeatSuccessfully() {
        Flight flight = createFlight(0);
        ReserveSeatCommand cmd = new ReserveSeatCommand(BOOKING_ID, FLIGHT_NUMBER, USER_ID);

        when(flightRepository.findByFlightNumberForUpdate(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));
        when(mappingRepository.findAllByFlightNumberForUpdate(FLIGHT_NUMBER)).thenReturn(Collections.emptyList());

        seatService.handle(cmd);

        verify(mappingRepository).save(any(BookingSeatMapping.class));
        verify(flightRepository).save(flight);
        verify(eventGateway).publish(any(SeatReservedEvent.class));
        verify(flightPublisher).publish(eq(FLIGHT_NUMBER), any(FlightUpdatedEvent.class));
    }

    @Test
    void shouldReserveMultipleSeatsInOrder() {
        Flight flight = createFlight(0);
        when(flightRepository.findByFlightNumberForUpdate(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));

        List<String> expectedSeatNumbers = List.of(
                "1A", "1B", "1C", "1D", "1E", "1F", "1G", "1H", "1J", "1K",
                "2A", "2B", "2C", "2D", "2E", "2F", "2G", "2H", "2J", "2K"
        );
        List<BookingSeatMapping> savedMappings = new ArrayList<>();

        when(mappingRepository.findAllByFlightNumberForUpdate(FLIGHT_NUMBER)).thenAnswer(invocation -> new ArrayList<>(savedMappings));

        doAnswer(invocation -> {
            BookingSeatMapping mapping = invocation.getArgument(0);
            savedMappings.add(mapping);
            return null;
        }).when(mappingRepository).save(any(BookingSeatMapping.class));

        for (int i = 0; i < 20; i++) {
            ReserveSeatCommand cmd = new ReserveSeatCommand(
                    BOOKING_ID + i,
                    FLIGHT_NUMBER,
                    USER_ID + i
            );
            seatService.handle(cmd);
        }

        assertEquals(20, savedMappings.size());

        List<String> actualSeatNumbers = savedMappings.stream()
                .map(BookingSeatMapping::getSeatNumber)
                .collect(Collectors.toList());

        assertEquals(expectedSeatNumbers, actualSeatNumbers );

        verify(flightRepository, times(20)).save(flight);
        verify(mappingRepository, times(20)).save(any(BookingSeatMapping.class));
        verify(eventGateway, times(20)).publish(any(SeatReservedEvent.class));
        verify(flightPublisher, times(20)).publish(eq(FLIGHT_NUMBER), any(FlightUpdatedEvent.class));
    }

    @Test
    void shouldFailReservationWhenNoSeats() {
        Flight flight = createFlight(200);
        ReserveSeatCommand cmd = new ReserveSeatCommand(BOOKING_ID, FLIGHT_NUMBER, USER_ID);
        when(flightRepository.findByFlightNumberForUpdate(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));

        seatService.handle(cmd);

        verify(eventGateway).publish(any(SeatReservationFailedEvent.class));
    }

    @Test
    void shouldReleaseSeatSuccessfully() {
        BookingSeatMapping mapping = BookingSeatMapping.builder()
                .bookingId(BOOKING_ID)
                .flightNumber(FLIGHT_NUMBER)
                .seatNumber("A1")
                .build();

        Flight flight = createFlight(50);

        when(mappingRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(mapping));
        when(flightRepository.findByFlightNumberForUpdate(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));

        seatService.handle(new ReleaseSeatCommand(BOOKING_ID));

        verify(flightRepository).save(any());
        verify(mappingRepository).delete(any());
        verify(flightPublisher).publish(eq(FLIGHT_NUMBER), any(FlightUpdatedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenNoAvailableSeatsToReserve() {
        Flight flight = createFlight(0);

        when(flightRepository.findByFlightNumberForUpdate(FLIGHT_NUMBER)).thenReturn(Optional.of(flight));
        when(mappingRepository.findAllByFlightNumberForUpdate(FLIGHT_NUMBER))
                .thenReturn(generateAllReservedSeats(50, new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K'}));

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