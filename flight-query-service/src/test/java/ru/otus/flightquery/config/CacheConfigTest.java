package ru.otus.flightquery.config;

import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CacheConfig.class)
class CacheConfigTest {

    @Autowired
    CacheConfig cacheConfig;

    @Autowired
    CacheManager cacheManager;

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setup() {
        meterRegistry = new SimpleMeterRegistry();
        cacheConfig.cacheMetrics(cacheManager).bindTo(meterRegistry);
    }

    @Test
    void shouldRegisterCacheMetricsAndReflectCacheUsage() {
        String cacheName = CacheConfig.ROUND_TRIP_FLIGHTS;
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
        CaffeineCache springCache = (CaffeineCache) caffeineCacheManager.getCache(cacheName);
        var nativeCache = springCache.getNativeCache();

        Gauge sizeGauge = meterRegistry.find("cache_size").tags(Tags.of("cache", cacheName)).gauge();
        Gauge hitGauge = meterRegistry.find("cache_requests_hit").tags(Tags.of("cache", cacheName)).gauge();
        Gauge missGauge = meterRegistry.find("cache_requests_miss").tags(Tags.of("cache", cacheName)).gauge();

        assertThat(sizeGauge).isNotNull();
        assertThat(hitGauge).isNotNull();
        assertThat(missGauge).isNotNull();

        // size should increment
        springCache.put("foo", "bar");

        // Cache hit
        Object value = nativeCache.getIfPresent("foo");
        assertThat(value).isEqualTo("bar");

        // Cache miss
        Object missValue = nativeCache.getIfPresent("baz");
        assertThat(missValue).isNull();

        assertThat(sizeGauge.value()).isEqualTo( 1);
        assertThat(hitGauge.value()).isEqualTo( 1);
        assertThat(missGauge.value()).isEqualTo(1);
    }
}