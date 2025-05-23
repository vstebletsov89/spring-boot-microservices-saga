package ru.otus.reservation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "outbox_events")
public class BookingOutboxEvent {

    @Id
    private UUID id;

    private String aggregateType;
    private String aggregateId;

    @Lob
    private String payload;

    private Instant createdAt;
    private boolean sent;
}