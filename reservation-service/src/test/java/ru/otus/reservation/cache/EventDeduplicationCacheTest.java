package ru.otus.reservation.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class EventDeduplicationCacheTest {
    private EventDeduplicationCache cache;

    @BeforeEach
    void setUp() {
        cache = new EventDeduplicationCache();
        cache.clear();
    }

    @Test
    void testIsDuplicate_FirstTime_ReturnsFalse() {
        String eventId = "event-1";

        assertFalse(cache.isDuplicate(eventId));
    }

    @Test
    void testIsDuplicate_SecondTime_ReturnsTrue() {
        String eventId = "event-1";

        assertFalse(cache.isDuplicate(eventId));
        assertTrue(cache.isDuplicate(eventId));
    }

    @Test
    void testClear_EmptiesCacheAndQueue() {
        var internalCache =
                (ConcurrentHashMap<String, Boolean>) ReflectionTestUtils.getField(cache, "cache");
        var internalQueue =
                (ConcurrentLinkedQueue<String>) ReflectionTestUtils.getField(cache, "queue");

        cache.isDuplicate("event-1");
        cache.isDuplicate("event-2");
        cache.clear();

        assertNotNull(internalCache);
        assertNotNull(internalQueue);
        assertTrue(internalCache.isEmpty());
        assertTrue(internalQueue.isEmpty());
    }

    @Test
    void testEviction_WhenCacheExceedsMaxSize() {
        ReflectionTestUtils.setField(cache, "MAX_CACHE_SIZE", 2);
        var internalCache =
                (ConcurrentHashMap<String, Boolean>) ReflectionTestUtils.getField(cache, "cache");
        var internalQueue =
                (ConcurrentLinkedQueue<String>) ReflectionTestUtils.getField(cache, "queue");

        cache.isDuplicate("event-1");
        cache.isDuplicate("event-2");
        cache.isDuplicate("event-3");

        assertFalse(internalCache.containsKey("event-1"));
        assertTrue(internalCache.containsKey("event-2"));
        assertTrue(internalCache.containsKey("event-3"));
        assertEquals(2, internalCache.size());
        assertEquals(2, internalQueue.size());
    }
}