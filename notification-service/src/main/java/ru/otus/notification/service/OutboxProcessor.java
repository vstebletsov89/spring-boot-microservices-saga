package ru.otus.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.notification.entity.NotificationOutbox;
import ru.otus.notification.repository.NotificationOutboxRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final NotificationOutboxRepository repo;
    private final DispatchService dispatchService;

    @Transactional
    public void processOne(UUID id) {
        NotificationOutbox o = repo.findById(id).orElseThrow();

        try {
            dispatchService.dispatch(o.getUserId(), o.getType(), o.getMessage());
            repo.markSent(id);
            log.info("SENT id={} userId={}", id, o.getUserId());
        } catch (Exception ex) {
            repo.markRetry(id);
            log.warn("RETRY id={} (attempt #{})", id, o.getRetryCount() + 1, ex);
        }
    }
}
