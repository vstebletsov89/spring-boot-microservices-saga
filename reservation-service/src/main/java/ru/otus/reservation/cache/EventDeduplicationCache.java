package ru.otus.reservation.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class EventDeduplicationCache {

    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private int MAX_CACHE_SIZE = 100_000;

    public boolean isDuplicate(String eventId) {

        if (cache.containsKey(eventId)) {
            log.info("{} already processed", eventId);
            return true;
        }

        cache.put(eventId, Boolean.TRUE);
        queue.offer(eventId);

        // remove the oldest event
        if (cache.size() > MAX_CACHE_SIZE) {
            String evictedId = queue.poll();
            if (evictedId != null) {
                log.info("{} removed from cache", eventId);
                cache.remove(evictedId);
            }
        }

        return false;
    }

    public void clear() {
        cache.clear();
        queue.clear();
        log.info("EventDeduplicationCache cleared.");
    }
}
