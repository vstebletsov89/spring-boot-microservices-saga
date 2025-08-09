package ru.otus.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.otus.notification.entity.NotificationOutbox;
import ru.otus.notification.repository.NotificationOutboxRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWriter {

    private final NotificationOutboxRepository repository;

    @Retryable(
            retryFor = {
                    org.springframework.dao.DataAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2.0, maxDelay = 2000, random = true)
    )
    public void save(NotificationOutbox notification) {
        repository.save(notification);
    }

    @Recover
    public void recover(Exception ex, NotificationOutbox n) {
        log.error("Cassandra write failed permanently for id={}", n.getId(), ex);
        throw new RuntimeException("Failed to save notification " + n.getId(), ex);
    }
}
