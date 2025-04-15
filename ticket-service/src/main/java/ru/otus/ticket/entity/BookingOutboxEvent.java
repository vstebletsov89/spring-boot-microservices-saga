package ru.otus.ticket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
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
    private String type;

    @Lob
    private String payload;

    private OffsetDateTime createdAt;
    private boolean sent;
}