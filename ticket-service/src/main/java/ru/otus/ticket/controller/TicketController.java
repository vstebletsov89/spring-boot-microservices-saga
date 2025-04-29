package ru.otus.ticket.controller;

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
        log.info("book ticket: {}", event);
        var response = bookingId + " booking created. Waiting for payment to confirm.";
        return ResponseEntity.ok(response);
    }
}

