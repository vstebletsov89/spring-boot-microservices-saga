package ru.otus.seatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "seat_inventory")
public class SeatInventory {

    @Id
    private String flightNumber;

    private double price;
    private int totalSeats;
    private int reservedSeats;
    private double overbookingPercentage;
}
