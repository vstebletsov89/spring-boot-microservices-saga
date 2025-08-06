package ru.otus.flightquery.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisEventDeduplicationCache {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration EVENT_TTL = Duration.ofHours(24);

    public boolean isDuplicate(String eventId) {
        Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(eventId, "1", EVENT_TTL);

        if (Boolean.FALSE.equals(wasSet)) {
            log.info("{} already processed", eventId);
            return true;
        }

        log.info("{} added to Redis cache with TTL {}", eventId, EVENT_TTL);
        return false;
    }
}
