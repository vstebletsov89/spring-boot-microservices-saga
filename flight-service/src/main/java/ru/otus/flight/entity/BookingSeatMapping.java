package ru.otus.flight.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.otus.common.enums.BookingStatus;

import java.time.OffsetDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "booking_seat_mapping",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_flight_seat",
                        columnNames = {"flight_number", "seat_number"})
        },
        indexes = {
                @Index(name = "idx_booking_id", columnList = "booking_id"),
                @Index(name = "idx_flight_number", columnList = "flight_number")
        }
)
public class BookingSeatMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingId;

    private String flightNumber;

    private String seatNumber;

    private OffsetDateTime reservedAt;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;


}
