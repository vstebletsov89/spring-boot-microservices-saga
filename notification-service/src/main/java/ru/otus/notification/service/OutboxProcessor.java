package ru.otus.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.notification.entity.NotificationOutbox;
import ru.otus.notification.repository.NotificationOutboxRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final NotificationOutboxRepository repository;
    private final DispatchService dispatchService;

    @Transactional
    public void processOne(UUID id) {
        log.info("Processing notification outbox {}", id);
        var notificationOutbox = repository.findById(id).orElseThrow();

        try {
            dispatchService.dispatch(notificationOutbox.getUserId(), notificationOutbox.getType(), notificationOutbox.getMessage());
            repository.markSent(id);
            log.info("SENT id={} userId={}", id, notificationOutbox.getUserId());
        } catch (Exception ex) {
            repository.markRetry(id);
            log.warn("RETRY id={} (attempt #{})", id, notificationOutbox.getRetryCount() + 1, ex);
        }
    }
}
