package ru.otus.ticketservice.controller;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.commands.BookFlightCommand;
import ru.otus.common.dto.BookingRequest;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private CommandGateway commandGateway;

    @PostMapping
    public CompletableFuture<String> createBooking(@RequestBody BookingRequest request) {
        String bookingId = UUID.randomUUID().toString();
        BookFlightCommand cmd = new BookFlightCommand(
                bookingId,
                request.userId(),
                request.flightNumber(),
                request.price()
        );
        //TODO: add write booking to pg db
        //TODO: send event to kafka
        //TODO: read kafka event and send command
        return commandGateway.send(cmd);
    }
}

