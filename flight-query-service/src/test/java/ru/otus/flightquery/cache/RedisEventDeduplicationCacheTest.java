package ru.otus.flightquery.cache;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

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
        String eventId = "event-single";

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
    void shouldHandleParallelRequests() throws Exception {
        String eventId = "event-parallel-1";
        int clientCount = 10;

        List<Boolean> results = runClientsConcurrently(clientCount, () -> cache.isDuplicate(eventId));

        long nonDuplicateCount = results.stream().filter(result -> !result).count();

        assertThat(nonDuplicateCount).isEqualTo(1);
    }

    private List<Boolean> runClientsConcurrently(int clientCount, Callable<Boolean> task) throws Exception {
            ExecutorService executor = Executors.newFixedThreadPool(clientCount);
            CountDownLatch startLatch = new CountDownLatch(1);

            List<Future<Boolean>> futures = IntStream.range(0, clientCount)
                    .mapToObj(i -> executor.submit(() -> {
                        startLatch.await();
                        return task.call();
                    }))
                    .toList();

            startLatch.countDown();

            executor.shutdown();

            return futures.stream()
                    .map(this::getFutureResult)
                    .toList();
    }

    private Boolean getFutureResult(Future<Boolean> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error executing task", e);
        }
    }
}
