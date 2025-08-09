package ru.otus.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.otus.notification.enums.NotificationStatus;
import ru.otus.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "notification_outbox",
        indexes = {
                @Index(name = "idx_outbox_status_next", columnList = "status,next_attempt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_outbox_event", columnNames = "event_id")
        }
)
public class NotificationOutbox {

    @Id
    private UUID id;

    private String eventId;

    private String userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int retryCount;

    private Instant lastAttempt;

    private Instant nextAttempt;
}
