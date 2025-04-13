package ru.otus.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.dto.BookingRequest;
import ru.otus.common.events.BookingCreatedEvent;
import ru.otus.ticketservice.service.TicketService;

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

//@RestController
//@RequestMapping("/api/bookings")
//public class TicketController {
//
//    @Autowired
//    private CommandGateway commandGateway;
//
//    @PostMapping
//    public CompletableFuture<String> createBooking(@RequestBody BookingRequest request) {
//        String bookingId = UUID.randomUUID().toString();
//        BookFlightCommand cmd = new BookFlightCommand(
//                bookingId,
//                request.userId(),
//                request.flightNumber(),
//                request.price()
//        );
//        //TODO: add write booking to pg db
//        //TODO: send event to kafka
//        //TODO: read kafka event and send command
//        return commandGateway.send(cmd);
//    }
//}

