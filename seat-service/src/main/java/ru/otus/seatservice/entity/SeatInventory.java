package ru.otus.seatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "flights")
public class SeatInventory {

    @Id
    private String flightNumber;

    private int totalSeats;
    private int reservedSeats;
}
