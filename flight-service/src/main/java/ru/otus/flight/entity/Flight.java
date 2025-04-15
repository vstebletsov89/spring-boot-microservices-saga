package ru.otus.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.otus.common.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "flights")
public class Flight {

    @Id
    private String flightNumber;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_airport_code", nullable = false)
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_airport_code", nullable = false)
    private Airport arrivalAirport;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    private ZonedDateTime departureTime;

    private ZonedDateTime arrivalTime;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    private int totalSeats;

    private int reservedSeats;

    @Column(precision = 5, scale = 2)
    private BigDecimal overbookingPercentage;

}
