package ru.otus.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.kafka.ReservationCancelledEvent;
import ru.otus.common.request.CancelReservationRequest;
import ru.otus.common.request.CreateReservationRequest;
import ru.otus.common.kafka.ReservationCreatedEvent;
import ru.otus.reservation.service.ReservationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flight Ticket Booking", description = "Endpoints for booking flight tickets")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "Book a flight ticket",
            description = "Creates a new booking request for a flight. Booking is not confirmed until payment is processed."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                         content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<String> bookTicket(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Booking request data including user ID and flight number",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateReservationRequest.class))
            )
            @RequestBody @Valid CreateReservationRequest request) {

        UUID bookingId = UUID.randomUUID();
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                UUID.randomUUID().toString(),
                request.userId(),
                request.flightNumber(),
                bookingId.toString(),
                request.seatNumber()
        );

        reservationService.createBookingRequest(event);
        log.info("book ticket: {}", event);
        var response = bookingId + " booking created. Waiting for payment to confirm.";
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancel a flight ticket",
            description = "Sends a request to cancel an existing flight reservation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelTicket(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cancellation request data including user ID, flight number, and booking ID",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CancelReservationRequest.class))
            )
            @RequestBody @Valid CancelReservationRequest request) {

        ReservationCancelledEvent event = new ReservationCancelledEvent(
                UUID.randomUUID().toString(),
                request.userId(),
                request.flightNumber(),
                request.bookingId()
        );

        reservationService.cancelBookingRequest(event);
        log.info("cancel ticket: {}", event);
        var response = request.bookingId() + " cancellation requested.";
        return ResponseEntity.ok(response);
    }
}

