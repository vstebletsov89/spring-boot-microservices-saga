package ru.otus.notification.repository;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProcessedEventDao {

    private final CqlSession session;

    private PreparedStatement psMarkProcessed;

    @PostConstruct
    public void init() {
        psMarkProcessed = session.prepare("""
            INSERT INTO notification_keyspace.processed_events (event_id, processed_at)
            VALUES (?, toTimestamp(now()))
            IF NOT EXISTS
        """);
    }

    public boolean tryMarkProcessed(String eventId) {
        BoundStatement bs = psMarkProcessed.bind(eventId)
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                .setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL)
                .setTimeout(Duration.ofSeconds(1));

        ResultSet rs = session.execute(bs);
        boolean applied = rs.wasApplied();

        log.info("LWT markProcessed eventId={} applied={}", eventId, applied);
        return applied;
    }
}
