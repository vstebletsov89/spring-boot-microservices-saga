package ru.otus.notification.entity;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import ru.otus.notification.enums.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("notification_outbox")
public class NotificationOutbox {

    @PrimaryKey
    private UUID id;

    private UUID notificationId;

    private NotificationStatus status;

    private int retryCount;

    private Instant lastAttempt;

    private Instant nextAttempt;

    private Instant createdAt;
}
