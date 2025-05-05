package ru.otus.flightquery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.response.BookingSeatMappingResponse;
import ru.otus.flightquery.service.BookingQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Queries", description = "Endpoints for retrieving booking information")
public class BookingQueryController {

    private final BookingQueryService bookingQueryService;

    @Operation(
            summary = "Get all bookings by flight number",
            description = "Returns all bookings for a specific flight number"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of bookings returned successfully"),
            @ApiResponse(responseCode = "404", description = "No bookings found for the given flight number")
    })
    @GetMapping("/flight/{flightNumber}")
    public ResponseEntity<List<BookingSeatMappingResponse>> getBookingsByFlightNumber(
            @Parameter(description = "Flight number to search for", required = true, example = "SU1234")
            @PathVariable String flightNumber) {
        log.info("Received request to get bookings for flight: {}", flightNumber);

        List<BookingSeatMappingResponse> bookings = bookingQueryService.findBookingsByFlightNumber(flightNumber);
        return ResponseEntity.ok(bookings);
    }


    @Operation(
            summary = "Get booking by booking ID",
            description = "Retrieves booking and seat assignment by unique booking ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingSeatMappingResponse> getBookingById(
            @Parameter(description = "Booking ID to search for", required = true, example = "627f2329-589e-436b-bb36-4474b3a5cc8e")
            @PathVariable String bookingId) {
        log.info("Received request to get booking by ID: {}", bookingId);

        BookingSeatMappingResponse booking = bookingQueryService.findBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }
}
