package ru.otus.flightquery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.entity.BookingSeatMapping;
import ru.otus.flightquery.service.BookingQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingQueryController {

    private final BookingQueryService bookingQueryService;

    @GetMapping("/flight/{flightNumber}")
    public ResponseEntity<List<BookingSeatMapping>> getBookingsByFlightNumber(@PathVariable String flightNumber) {
        log.info("Received request to get bookings for flight: {}", flightNumber);
        List<BookingSeatMapping> bookings = bookingQueryService.findBookingsByFlightNumber(flightNumber);
        return ResponseEntity.ok(bookings);
    }


    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingSeatMapping> getBookingById(@PathVariable String bookingId) {
        log.info("Received request to get booking by ID: {}", bookingId);
        BookingSeatMapping booking = bookingQueryService.findBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }
}
