package ru.otus.flightquery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.otus.common.enums.FlightStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;

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


    private String departureAirportCode;

    private String arrivalAirportCode;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    @Column(precision = 19, scale = 4)
    private BigDecimal price;

    private int totalSeats;

    private int reservedSeats;

    @Column(precision = 5, scale = 2)
    private BigDecimal overbookingPercentage;

}
