package ru.otus.flightquery.cache;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RedisEventDeduplicationCacheTest {

    static RedisContainer redis;
    RedisTemplate<String, String> redisTemplate;
    RedisEventDeduplicationCache cache;

    @BeforeAll
    static void startRedis() {
        redis = new RedisContainer("redis:7.2-alpine");
        redis.start();
    }

    @AfterAll
    static void stopRedis() {
        redis.stop();
    }

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(redis.getHost(), redis.getFirstMappedPort());
        connectionFactory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();

        cache = new RedisEventDeduplicationCache(redisTemplate);
    }

    @Test
    void shouldDetectDuplicateCorrectly() {
        String eventId = "event-1";

        assertThat(cache.isDuplicate(eventId)).isFalse();
        assertThat(cache.isDuplicate(eventId)).isTrue();
    }

    @Test
    void shouldHandleMultipleEvents() {
        assertThat(cache.isDuplicate("event-1")).isFalse();
        assertThat(cache.isDuplicate("event-2")).isFalse();
        assertThat(cache.isDuplicate("event-1")).isTrue();
        assertThat(cache.isDuplicate("event-2")).isTrue();
    }

    @Test
    void shouldHandleParallelRequests() {
        //TODO:
    }
}
