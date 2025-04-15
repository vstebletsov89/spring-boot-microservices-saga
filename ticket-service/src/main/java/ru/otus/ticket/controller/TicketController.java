package ru.otus.ticket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.request.BookingRequest;
import ru.otus.common.event.BookingCreatedEvent;
import ru.otus.ticket.service.TicketService;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<String> bookTicket(@RequestBody BookingRequest request) {
        UUID bookingId = UUID.randomUUID();
        BookingCreatedEvent event = new BookingCreatedEvent(
                request.userId(),
                request.flightNumber(),
                bookingId.toString()
        );
        ticketService.createBookingRequest(event);
        return ResponseEntity.ok("Booking request accepted. Waiting confirmation.");
    }
}

