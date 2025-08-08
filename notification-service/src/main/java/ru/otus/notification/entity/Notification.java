package ru.otus.notification.entity;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import ru.otus.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("notifications")
public class Notification {

    @PrimaryKey
    private UUID id;

    private String userId;

    private NotificationType type;

    private String message;

    private Instant createdAt;
}
