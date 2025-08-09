package ru.otus.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.notification.repository.NotificationOutboxRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxScheduler {

    private final NotificationOutboxRepository repo;
    private final OutboxProcessor processor;
    private final Executor outboxExecutor;

    private static final int BATCH_LIMIT = 500;

    @Scheduled(fixedDelayString = "PT1M")
    @SchedulerLock(name = "notification-outbox-scheduler", lockAtMostFor = "PT1M", lockAtLeastFor = "PT10S")
    @Transactional
    public void pollAndSchedule() {
        List<UUID> ids = repo.lockBatchForProcessing(BATCH_LIMIT);
        if (ids.isEmpty()) {
            log.debug("Outbox: no due items");
            return;
        }

        log.info("Outbox: scheduling {} tasks", ids.size());
        for (UUID id : ids) {
            CompletableFuture
                    .runAsync(() -> processor.processOne(id), outboxExecutor)
                    .exceptionally(ex -> { log.error("Async task failed id={}", id, ex); return null; });
        }
    }
}
