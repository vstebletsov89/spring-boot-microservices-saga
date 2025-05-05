package ru.otus.ticket.controller;

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
import ru.otus.common.request.BookingRequest;
import ru.otus.common.saga.BookingCreatedEvent;
import ru.otus.ticket.service.TicketService;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flight Ticket Booking", description = "Endpoints for booking flight tickets")
public class TicketController {

    private final TicketService ticketService;

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
                    content = @Content(schema = @Schema(implementation = BookingRequest.class))
            )
            @RequestBody @Valid BookingRequest request) {
        UUID bookingId = UUID.randomUUID();
        BookingCreatedEvent event = new BookingCreatedEvent(
                request.userId(),
                request.flightNumber(),
                bookingId.toString()
        );
        ticketService.createBookingRequest(event);
        log.info("book ticket: {}", event);
        var response = bookingId + " booking created. Waiting for payment to confirm.";
        return ResponseEntity.ok(response);
    }
}

