package ru.otus.notification.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.notification.entity.NotificationOutbox;
import ru.otus.notification.enums.NotificationStatus;
import ru.otus.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationOutboxMapper {

    private final NotificationType defaultType;

    public NotificationOutboxMapper(@Value("${app.notification.default-type}") NotificationType defaultType) {
        this.defaultType = defaultType;
    }

    public NotificationOutbox toOutbox(PaymentEvent e) {
        return NotificationOutbox.builder()
                .id(UUID.randomUUID())
                .eventId(e.eventId())
                .userId(e.userId())
                .type(defaultType)
                .message(formatMessage(e))
                .createdAt(e.occurredAt())
                .status(NotificationStatus.NEW)
                .retryCount(0)
                .lastAttempt(null)
                .nextAttempt(Instant.now())
                .build();
    }

    private String formatMessage(PaymentEvent e) {
        return switch (e.status()) {
            case FAILED, REFUND_FAILED ->
                    String.format(e.status().getMessageTemplate(), e.bookingId(), e.failureReason());
            default ->
                    String.format(e.status().getMessageTemplate(), e.bookingId(), e.amount());
        };
    }
}
