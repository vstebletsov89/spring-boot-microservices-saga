package ru.otus.ticketservice.controller;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.common.dto.BookingRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final KafkaProducer<String, BookingRequest> kafkaProducer;

    @Value("${app.kafka.topic.inbound}")
    private String inboundTopic;

    @PostMapping
    public String createBooking(@RequestBody BookingRequest request) {
        String bookingId = UUID.randomUUID().toString();
        kafkaProducer.send(new ProducerRecord<>(inboundTopic, bookingId, request));
        return "Booking request sent. Waiting confirmation";
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

