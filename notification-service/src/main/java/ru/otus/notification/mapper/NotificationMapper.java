package ru.otus.notification.mapper;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.common.kafka.PaymentEvent;
import ru.otus.notification.entity.NotificationOutbox;
import ru.otus.notification.enums.NotificationStatus;
import ru.otus.notification.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationMapper {

    private final NotificationType defaultType;

    public NotificationMapper(@Value("${app.notification.default-type}") NotificationType defaultType) {
        this.defaultType = defaultType;
    }

    public NotificationOutbox toOutbox(PaymentEvent event) {
        String msg = formatMessage(event);
        UUID id = Uuids.timeBased();
        Instant now = Instant.now();

        return NotificationOutbox.builder()
                .id(id)
                .userId(event.userId())
                .type(defaultType)
                .message(msg)
                .createdAt(event.occurredAt())
                .status(NotificationStatus.NEW)
                .retryCount(0)
                .lastAttempt(null)
                .nextAttempt(now)
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
