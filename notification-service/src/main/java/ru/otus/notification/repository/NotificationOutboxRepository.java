package ru.otus.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.notification.entity.NotificationOutbox;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

    @Query(value = """
        SELECT id FROM notification_outbox
         WHERE status IN ('NEW','RETRYING')
           AND next_attempt <= now()
         ORDER BY next_attempt
         FOR UPDATE SKIP LOCKED
         LIMIT :limit
        """, nativeQuery = true)
    List<UUID> lockBatchForProcessing(@Param("limit") int limit);

    @Modifying
    @Query(value = """
        UPDATE notification_outbox
           SET status = 'SENT',
               last_attempt = now()
         WHERE id = :id
        """, nativeQuery = true)
    int markSent(@Param("id") UUID id);

    @Modifying
    @Query(value = """
        UPDATE notification_outbox
           SET status = 'RETRYING',
               retry_count = retry_count + 1,
               last_attempt = now()
         WHERE id = :id
        """, nativeQuery = true)
    int markRetry(@Param("id") UUID id);
}
