package ru.otus.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.otus.common.response.BookingDetailsResponse;
import ru.otus.reservation.service.BookingQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking details", description = "Endpoints for retrieving flight ticket reservations")
public class BookingQueryController {

    private final BookingQueryService bookingQueryService;

    @Operation(summary = "Get booking details by ID", description = "Returns a single booking by booking ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<BookingDetailsResponse> getByBookingId(@PathVariable String bookingId) {

        log.info("Fetching booking with ID {}", bookingId);
        return bookingQueryService.getByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
