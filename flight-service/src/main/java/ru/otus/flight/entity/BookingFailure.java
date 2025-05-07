package ru.otus.flight.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "booking_failures")
public class BookingFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String bookingId;

    private String flightNumber;

    private String reason;

    @Lob
    private String payload;

    private Instant attemptTime;
}
